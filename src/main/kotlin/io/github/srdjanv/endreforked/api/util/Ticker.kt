package io.github.srdjanv.endreforked.api.util

import java.util.concurrent.ThreadLocalRandom

/** Ticks once every [frequency] calls. The frequency is read on each call, so a script can change it. */
class Ticker(private val frequencyOf: () -> Int) {
    constructor(frequency: Int) : this({ frequency })

    val frequency: Int get() = frequencyOf()
    private var tick = ThreadLocalRandom.current().nextInt(maxOf(1, frequency))

    fun tick(): Boolean = tick++ % maxOf(1, frequency) == 0
}
