package io.github.srdjanv.endreforked.api.capabilities.entropy

interface WeakEntropyStorage : EntropyStorage {
    var loadFactor: Double
    val isOverLoaded: Boolean

    /** The amount of entropy lost for each inversion/extraction if the storage exceeds the load factor. */
    var decay: Int
}
