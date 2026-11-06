package io.github.srdjanv.endreforked.impl.registry

import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.api.registry.LoadReport
import io.github.srdjanv.endreforked.api.registry.Registry
import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import io.github.srdjanv.endreforked.api.registry.RegistryKey
import io.github.srdjanv.endreforked.api.registry.RegistryScripts
import java.util.concurrent.CopyOnWriteArrayList

/** An immutable registry. The entries are ordered by id, so iteration is deterministic. */
open class FrozenRegistry<E : RegistryEntry>(override val key: RegistryKey<E>, entries: Collection<E>) : Registry<E> {
    private val byId: Map<Id, E> = entries.sortedBy { it.id }.associateByTo(LinkedHashMap()) { it.id }
    override val entries: Collection<E> = byId.values

    override fun get(id: Id): E? = byId[id]
}

/**
 * The one lifecycle of every registry. A rebuild applies ordered layers to an empty map:
 * 1. code defaults, 2. scripts ([RegistryScripts]).
 * Each layer is a list of operations: put (add or replace), remove by id, remove by predicate.
 * Then every entry is validated, a new immutable snapshot is built ([current]) and it replaces the old one atomically.
 * Readers see either the old or the new snapshot. Listeners are called after the swap.
 *
 * An invalid entry is reported, skipped, and never stops a rebuild. Before the first rebuild [current] is empty.
 */
class ManagedRegistry<E : RegistryEntry, R : Registry<E>>(
    val key: RegistryKey<E>,
    private val defaults: () -> Collection<E>,
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

    @Synchronized
    override fun rebuild(): LoadReport {
        val entries = LinkedHashMap<Id, E>()
        val errors = ArrayList<String>()
        val applied = LinkedHashMap<String, Int>()
        applied["defaults"] = applyOps(entries, defaults().map { Op.Put("code defaults", it) }, errors)
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

    private companion object {
        const val SCRIPTS = "scripts"
    }
}
