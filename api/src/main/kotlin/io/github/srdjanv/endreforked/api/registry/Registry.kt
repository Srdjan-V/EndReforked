package io.github.srdjanv.endreforked.api.registry

/** A namespaced id, written `namespace:path`. */
data class Id(val namespace: String, val path: String) : Comparable<Id> {
    override fun toString() = "$namespace:$path"

    override fun compareTo(other: Id) = toString().compareTo(other.toString())

    companion object {
        private val VALID = Regex("[a-z0-9_.-]+:[a-z0-9_./-]+")

        /** Returns null when [text] is not a lower-case `namespace:path`. */
        fun parseOrNull(text: String): Id? =
            if (VALID.matches(text)) Id(text.substringBefore(':'), text.substringAfter(':')) else null

        fun parse(text: String): Id = parseOrNull(text) ?: throw IllegalArgumentException("Invalid id '$text', expected namespace:path")
    }
}

/** Anything that lives in a [Registry]. */
interface RegistryEntry {
    val id: Id
}

/** The id of a registry, for example `endreborn:machine_recipes`. [E] is the entry type. */
data class RegistryKey<E : RegistryEntry>(val id: Id) {
    override fun toString() = id.toString()
}

/**
 * Read-only view of one registry snapshot. A snapshot never changes.
 * Each registry adds its own typed lookup on top of this interface.
 */
interface Registry<E : RegistryEntry> : Iterable<E> {
    val key: RegistryKey<E>
    val entries: Collection<E>
    val size: Int get() = entries.size

    operator fun get(id: Id): E?

    override fun iterator(): Iterator<E> = entries.iterator()
}

/** What a rebuild did. [applied] counts the operations that changed something, per layer. [errors] has one line per problem. */
data class LoadReport(val total: Int, val applied: Map<String, Int>, val errors: List<String>)

/**
 * The script layer of a registry. CraftTweaker and GroovyScript adapters use it, and they convert their stacks to ids first.
 *
 * The layer is the last of two, applied in order on every rebuild: code defaults, scripts.
 * Operations are only recorded. Nothing becomes visible until [rebuild], which applies all layers and swaps the snapshot.
 * Before the first rebuild this is how scripts that run early (CraftTweaker at init) add their operations.
 * After the first rebuild a script reload calls [clear], runs the scripts again, then [rebuild] (GroovyScript `/gs reload`).
 * Scripted entries are validated like any other and are synced to clients like any other.
 */
interface RegistryScripts<E : RegistryEntry> {
    /** Adds an entry, or replaces the one with the same id. */
    fun add(entry: E)

    fun remove(id: Id)

    /** Removes every entry the predicate accepts, from the result of the earlier operations. */
    fun removeIf(predicate: (E) -> Boolean)

    /** Drops all recorded script operations. */
    fun clear()

    /** Applies all layers, validates, and swaps the snapshot. Listeners are called. */
    fun rebuild(): LoadReport
}
