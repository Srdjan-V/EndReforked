package io.github.srdjanv.endreforked.common.entropy

import io.github.srdjanv.endreforked.api.capabilities.timedflight.ITimedFlight
import kotlin.math.min

/** The pure rules of the entropy cell item. */
object EntropyCell {
    /** What a cell with [content] takes from a chunk that offers [available]. */
    fun fill(content: Int, capacity: Int, available: Int) = min(available, (capacity - content).coerceAtLeast(0)).coerceAtLeast(0)

    /** What a cell with [content] gives to a chunk that accepts [accepted]. */
    fun empty(content: Int, accepted: Int) = min(content, accepted).coerceAtLeast(0)

    /** Extends an expiring flight by [ticks] if [pay] takes [cost] entropy from a cell. Returns true if it extended. */
    fun extendFlight(flight: ITimedFlight, cost: Int, ticks: Int, pay: (Int) -> Boolean): Boolean {
        if (!flight.isExpiring || ticks <= 0 || !pay(cost)) return false
        flight.flightDuration = ticks
        return true
    }
}
