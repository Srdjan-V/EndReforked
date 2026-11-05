package io.github.srdjanv.endreforked.impl.registry

import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.api.registry.LoadReport
import io.github.srdjanv.endreforked.api.registry.Registry
import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import io.github.srdjanv.endreforked.api.registry.RegistryKey
import io.github.srdjanv.endreforked.api.registry.RegistryScripts
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText

/** An immutable registry. The entries are ordered by id, so iteration is deterministic. */
open class FrozenRegistry<E : RegistryEntry>(override val key: RegistryKey<E>, entries: Collection<E>) : Registry<E> {
    private val byId: Map<Id, E> = entries.sortedBy { it.id }.associateByTo(LinkedHashMap()) { it.id }
    override val entries: Collection<E> = byId.values

    override fun get(id: Id): E? = byId[id]
}

/** Reads one entry from a JSON object. Throws when the object is invalid. The "remove" key never reaches the codec. */
fun interface EntryCodec<E : RegistryEntry> {
    fun decode(json: JsonObject): E
}

/**
 * The one lifecycle of every registry. A rebuild applies ordered layers to an empty map:
 * 1. code defaults, 2. JSON files, 3. scripts ([RegistryScripts]).
 * Each layer is a list of operations: put (add or replace), remove by id, remove by predicate.
 * Then every entry is validated, a new immutable snapshot is built ([current]) and it replaces the old one atomically.
 * Readers see either the old or the new snapshot. Listeners are called after the swap.
 *
 * The JSON layer is read from disk by [reload] only. [rebuild] reuses the last read, so a JSON reload keeps the script layer
 * and a script change does not touch the disk. Before the first rebuild [current] is empty.
 *
 * JSON files (`*.json`, read in path order): one file holds one object or an array of objects.
 * An object with the id of an existing entry replaces it. `{"id": "...", "remove": true}` removes an entry.
 * An invalid entry is reported, skipped, and never stops a rebuild.
 */
class ManagedRegistry<E : RegistryEntry, R : Registry<E>>(
    val key: RegistryKey<E>,
    private val defaults: () -> Collection<E>,
    private val codec: EntryCodec<E>,
    private val validate: (E) -> String?,
    private val freeze: (Collection<E>) -> R,
) : RegistryScripts<E> {
    private sealed interface Op {
        val source: String

        class Put<E>(override val source: String, val entry: E) : Op

        class Remove(override val source: String, val id: Id) : Op

        class RemoveIf<E>(override val source: String, val predicate: (E) -> Boolean) : Op
    }

    @Volatile
    var current: R = freeze(emptyList())
        private set

    /** False until the first rebuild. Script operations are allowed in both states. */
    @Volatile
    var isFrozen = false
        private set

    private val listeners = CopyOnWriteArrayList<(R) -> Unit>()
    private var jsonOps: List<Op> = emptyList()
    private val scriptOps = ArrayList<Op>()

    /** Called with every new snapshot, on the thread that replaced it. */
    fun addListener(listener: (R) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (R) -> Unit) {
        listeners.remove(listener)
    }

    @Synchronized
    override fun add(entry: E) {
        scriptOps += Op.Put(SCRIPTS, entry)
    }

    @Synchronized
    override fun remove(id: Id) {
        scriptOps += Op.Remove(SCRIPTS, id)
    }

    @Synchronized
    override fun removeIf(predicate: (E) -> Boolean) {
        scriptOps += Op.RemoveIf(SCRIPTS, predicate)
    }

    @Synchronized
    override fun clear() {
        scriptOps.clear()
    }

    /** Reads the JSON files under [dir] (may be null or missing), then rebuilds. */
    @Synchronized
    fun reload(dir: Path?): LoadReport {
        val errors = ArrayList<String>()
        jsonOps = if (dir != null && Files.isDirectory(dir)) readJson(dir, errors) else emptyList()
        val report = rebuild()
        return report.copy(errors = errors + report.errors)
    }

    @Synchronized
    override fun rebuild(): LoadReport {
        val entries = LinkedHashMap<Id, E>()
        val errors = ArrayList<String>()
        val applied = LinkedHashMap<String, Int>()
        applied["defaults"] = applyOps(entries, defaults().map { Op.Put("code defaults", it) }, errors)
        applied["json"] = applyOps(entries, jsonOps, errors)
        applied["scripts"] = applyOps(entries, scriptOps, errors)
        isFrozen = true
        replace(entries.values)
        return LoadReport(entries.size, applied, errors)
    }

    /** Replaces the snapshot with [entries] without layers or validation. Used by the client for server snapshots. */
    fun replace(entries: Collection<E>) {
        val snapshot = freeze(entries)
        current = snapshot
        listeners.forEach { it(snapshot) }
    }

    private fun applyOps(entries: MutableMap<Id, E>, ops: List<Op>, errors: MutableList<String>): Int {
        var changed = 0
        for (op in ops) {
            when (op) {
                is Op.Put<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val entry = op.entry as E
                    val problem = validate(entry)
                    if (problem != null) {
                        errors += "${op.source}: ${entry.id}: $problem"
                    } else {
                        entries[entry.id] = entry
                        changed++
                    }
                }
                is Op.Remove -> if (entries.remove(op.id) != null) changed++ else errors += "${op.source}: ${op.id}: nothing to remove"
                is Op.RemoveIf<*> -> try {
                    @Suppress("UNCHECKED_CAST")
                    val test = op.predicate as (E) -> Boolean
                    val ids = entries.values.filter(test).map { it.id }
                    ids.forEach(entries::remove)
                    changed += ids.size
                } catch (e: Exception) {
                    errors += "${op.source}: remove by predicate failed: ${e.message}"
                }
            }
        }
        return changed
    }

    private fun readJson(dir: Path, errors: MutableList<String>): List<Op> {
        val ops = ArrayList<Op>()
        val files = Files.walk(dir).use { s -> s.filter { it.isRegularFile() && it.name.endsWith(".json") }.sorted().toList() }
        for (file in files) {
            val name = dir.relativize(file).toString()
            val root = try {
                Json.parseToJsonElement(file.readText())
            } catch (e: Exception) {
                errors += "$name: not valid JSON: ${e.message}"
                continue
            }
            for (element in (root as? JsonArray) ?: JsonArray(listOf(root))) {
                val obj = element as? JsonObject
                if (obj == null) {
                    errors += "$name: entry is not a JSON object"
                    continue
                }
                val id = (obj["id"] as? JsonPrimitive)?.contentOrNull?.let(Id::parseOrNull)
                if (id == null) {
                    errors += "$name: entry has no valid 'id'"
                } else if ((obj["remove"] as? JsonPrimitive)?.booleanOrNull == true) {
                    ops += Op.Remove(name, id)
                } else try {
                    ops += Op.Put(name, codec.decode(JsonObject(obj - "remove")))
                } catch (e: Exception) {
                    errors += "$name: $id: ${e.message}"
                }
            }
        }
        return ops
    }

    private companion object {
        const val SCRIPTS = "scripts"
    }
}
