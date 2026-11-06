package io.github.srdjanv.endreforked.api.util

import java.util.concurrent.ThreadLocalRandom

class Ticker(val frequency: Int) {
    private var tick = ThreadLocalRandom.current().nextInt(frequency)

    fun tick(): Boolean = tick++ % frequency == 0
}
