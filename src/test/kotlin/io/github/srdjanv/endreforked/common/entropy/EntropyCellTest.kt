package io.github.srdjanv.endreforked.common.entropy

import io.github.srdjanv.endreforked.api.capabilities.timedflight.ITimedFlight
import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import net.minecraft.entity.player.EntityPlayerMP
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EntropyCellTest {
    @AfterEach
    fun reset() = EntropyTuning.reset()

    private class Flight(var left: Int) : ITimedFlight {
        override var flightDuration: Int
            get() = left
            set(value) { if (value > 0) left = value }
        override val isExpiring get() = left <= 0
        override fun tickPlayer(playerMP: EntityPlayerMP) {}
    }

    @Test
    fun flightDurationScalesWithLevel() {
        assertEquals(1000, EntropyTuning.flightDuration(1000, EntropyLevel.STABLE))
        assertEquals(1500, EntropyTuning.flightDuration(1000, EntropyLevel.CHARGED))
        assertEquals(2000, EntropyTuning.flightDuration(1000, EntropyLevel.CRITICAL))
        EntropyTuning.flightCriticalFactor = 3.0
        assertEquals(3000, EntropyTuning.flightDuration(1000, EntropyLevel.CRITICAL))
    }

    @Test
    fun fillMath() {
        assertEquals(300, EntropyCell.fill(0, 1000, 300))
        assertEquals(100, EntropyCell.fill(900, 1000, 500))
        assertEquals(0, EntropyCell.fill(1000, 1000, 500))
    }

    @Test
    fun extensionUsesEntropyAndStopsWhenEmpty() {
        var cell = 250
        val pay = { cost: Int -> (cell >= cost).also { if (it) cell -= cost } }
        val flight = Flight(0)
        assertTrue(EntropyCell.extendFlight(flight, 100, 400, pay))
        assertEquals(400, flight.left)
        flight.left = 0
        assertTrue(EntropyCell.extendFlight(flight, 100, 400, pay))
        flight.left = 0
        assertFalse(EntropyCell.extendFlight(flight, 100, 400, pay))
        assertEquals(50, cell)
        assertEquals(0, flight.left)
        flight.left = 5
        assertFalse(EntropyCell.extendFlight(flight, 10, 400, pay))
        assertEquals(50, cell)
    }
}
