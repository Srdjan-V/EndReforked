package io.github.srdjanv.endreforked.common.machine

/** Turns a speed like 1.5 into whole ticks of progress: the fraction is kept for the next tick. */
class ProgressAccumulator {
    private var fraction = 0.0

    fun step(speed: Double): Int {
        fraction += speed
        val whole = fraction.toInt()
        fraction -= whole
        return whole
    }
}
