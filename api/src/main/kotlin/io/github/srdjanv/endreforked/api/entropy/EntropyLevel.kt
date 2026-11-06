package io.github.srdjanv.endreforked.api.entropy

/** How full a chunk is with entropy. The server computes it, the client gets the ordinal in the ChunkEntropy message. */
enum class EntropyLevel {
    STABLE, CHARGED, CRITICAL;

    companion object {
        /** Stable below [chargedPercent] of [capacity], critical above [criticalPercent], else charged. A capacity of 0 is stable. */
        fun of(entropy: Int, capacity: Int, chargedPercent: Int, criticalPercent: Int): EntropyLevel {
            val scaled = entropy.toLong() * 100
            return when {
                capacity <= 0 || scaled < chargedPercent.toLong() * capacity -> STABLE
                scaled <= criticalPercent.toLong() * capacity -> CHARGED
                else -> CRITICAL
            }
        }
    }
}
