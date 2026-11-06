package io.github.srdjanv.endreforked.api.capabilities.entropy

interface EntropyStorage {
    val maxEntropy: Int
    val currentEntropy: Int

    /** If this is false, any call to [induceEntropy] returns 0. */
    fun canInduceEntropy(): Boolean = currentEntropy < maxEntropy

    /** @return the amount of entropy the storage accepted. [simulate] only simulates the fill. */
    fun induceEntropy(entropy: Int, simulate: Boolean): Int

    /** If this is false, any call to [drainEntropy] returns 0. */
    fun canDrainEntropy(): Boolean = currentEntropy != 0

    /** @return the amount that was (or would have been, if [simulate]) drained. */
    fun drainEntropy(entropy: Int, simulate: Boolean): Int
}
