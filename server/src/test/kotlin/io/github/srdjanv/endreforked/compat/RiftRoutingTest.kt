package io.github.srdjanv.endreforked.compat

import io.github.srdjanv.endreforked.compat.tconstruct.RiftRouting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class RiftRoutingTest {
    /** A sink that takes up to [free] of an item count and returns the rest, or null. */
    private fun sink(free: Int): (Int) -> Int? = { n -> (n - free).takeIf { it > 0 } }

    @Test
    fun routesInOrderAndKeepsTheRest() {
        assertNull(RiftRouting.route(5, listOf(sink(10), sink(10))))
        assertNull(RiftRouting.route(15, listOf(sink(10), sink(10))))
        assertEquals(5, RiftRouting.route(25, listOf(sink(10), sink(10))))
        assertEquals(7, RiftRouting.route(7, emptyList()))
    }
}
