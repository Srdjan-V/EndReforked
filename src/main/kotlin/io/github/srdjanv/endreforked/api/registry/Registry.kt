package io.github.srdjanv.endreforked.api.registry

import net.minecraft.util.ResourceLocation

/** Anything that lives in a [Registry]. */
interface RegistryEntry {
    val id: ResourceLocation
}

/**
 * Read-only view of one registry snapshot. A snapshot never changes.
 * Each registry adds its own typed lookup on top of this interface.
 */
interface Registry<E : RegistryEntry> : Iterable<E> {
    /** The id of the registry, for example `endreborn:machine_recipes`. */
    val key: ResourceLocation
    val entries: Collection<E>
    val size: Int get() = entries.size

    operator fun get(id: ResourceLocation): E?

    override fun iterator(): Iterator<E> = entries.iterator()
}

/** What a rebuild did. [applied] counts the operations that changed something, per layer. [errors] has one line per problem. */
data class LoadReport(val total: Int, val applied: Map<String, Int>, val errors: List<String>)

/**
 * The script layer of a registry. CraftTweaker and GroovyScript adapters use it and pass Minecraft objects.
 *
 * The layer is the last of two, applied in order on every rebuild: code defaults, scripts.
 * Operations are only recorded. Nothing becomes visible until [rebuild], which applies all layers and swaps the snapshot.
 * Before the first rebuild this is how scripts that run early (CraftTweaker) add their operations.
 * After the first rebuild a script reload calls [clear], runs the scripts again, then [rebuild] (GroovyScript `/gs reload`).
 * Every operation has an owner (one script engine). [clear] with an owner drops only the operations of that owner, so a reload of one engine keeps the others.
 * Scripted entries are validated like any other and are synced to clients like any other.
 */
interface RegistryScripts<E : RegistryEntry> {
    /** Adds an entry, or replaces the one with the same id. */
    fun add(entry: E, owner: String = "scripts")

    fun remove(id: ResourceLocation, owner: String = "scripts")

    /** Removes every entry the predicate accepts, from the result of the earlier operations. */
    fun removeIf(owner: String = "scripts", predicate: (E) -> Boolean)

    /** Drops all recorded script operations. */
    fun clear()

    /** Drops the recorded script operations of [owner] only. */
    fun clear(owner: String)

    /** Applies all layers, validates, and swaps the snapshot. Listeners are called. */
    fun rebuild(): LoadReport
}
