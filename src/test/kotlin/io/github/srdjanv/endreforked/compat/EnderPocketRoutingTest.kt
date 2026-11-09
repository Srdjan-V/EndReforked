package io.github.srdjanv.endreforked.compat

import io.github.srdjanv.endreforked.compat.tconstruct.EnderPocketRouting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class EnderPocketRoutingTest {
    /** A sink that takes up to [free] of an item count and returns the rest, or null. */
    private fun sink(free: Int): (Int) -> Int? = { n -> (n - free).takeIf { it > 0 } }

    @Test
    fun routesInOrderAndKeepsTheRest() {
        assertNull(EnderPocketRouting.route(5, listOf(sink(10), sink(10))))
        assertNull(EnderPocketRouting.route(15, listOf(sink(10), sink(10))))
        assertEquals(5, EnderPocketRouting.route(25, listOf(sink(10), sink(10))))
        assertEquals(7, EnderPocketRouting.route(7, emptyList()))
    }
}
