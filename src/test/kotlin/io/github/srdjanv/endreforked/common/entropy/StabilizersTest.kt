package io.github.srdjanv.endreforked.common.entropy

import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StabilizersTest {
    @AfterEach
    fun reset() = EntropyTuning.reset()

    @Test
    fun theTierSetsTheArea() {
        assertTrue(Stabilizers.covers(0, 0, 0))
        assertFalse(Stabilizers.covers(0, 1, 0))
        assertTrue(Stabilizers.covers(1, -1, 1))
        assertFalse(Stabilizers.covers(1, 2, 0))
        assertTrue(Stabilizers.covers(2, 2, -2))
        assertFalse(Stabilizers.covers(2, 0, 3))
    }

    @Test
    fun theCapKeepsTheChunkBelowCritical() {
        assertEquals(800, EntropyTuning.stabilizerCap(1000)) // follows criticalPercent (80)
        assertEquals(200, Stabilizers.room(600, 800))
        assertEquals(0, Stabilizers.room(900, 800))
        EntropyTuning.criticalPercent = 70
        assertEquals(700, EntropyTuning.stabilizerCap(1000))
        EntropyTuning.stabilizerCapPercent = 50
        assertEquals(500, EntropyTuning.stabilizerCap(1000))
    }

    @Test
    fun aChunkAtTheCapIsStillCharged() {
        val cap = EntropyTuning.stabilizerCap(1000)
        assertEquals(EntropyLevel.CHARGED, EntropyTuning.level(cap, 1000))
    }
}
