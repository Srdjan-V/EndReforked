package io.github.srdjanv.endreforked.common.entropy

import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import io.github.srdjanv.endreforked.common.machine.ProgressAccumulator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EntropyLevelTest {
    @Test
    fun levelThresholds() {
        fun level(e: Int) = EntropyTuning.level(e, 1000)
        assertEquals(EntropyLevel.STABLE, level(0))
        assertEquals(EntropyLevel.STABLE, level(329))
        assertEquals(EntropyLevel.CHARGED, level(330))
        assertEquals(EntropyLevel.CHARGED, level(800))
        assertEquals(EntropyLevel.CRITICAL, level(801))
        assertEquals(EntropyLevel.STABLE, EntropyTuning.level(5, 0))
    }

    @Test
    fun levelUsesTheTuning() {
        EntropyTuning.chargedPercent = 10
        try {
            assertEquals(EntropyLevel.CHARGED, EntropyTuning.level(100, 1000))
            EntropyTuning.reset()
            assertEquals(EntropyLevel.STABLE, EntropyTuning.level(100, 1000))
        } finally {
            EntropyTuning.reset()
        }
    }

    @Test
    fun decayMovesTowardsBaseAndNeverPast() {
        assertEquals(95, EntropyTuning.decayStep(100, 0, 5))
        assertEquals(2, EntropyTuning.decayStep(100, 2, 500))
        assertEquals(505, EntropyTuning.decayStep(500, 600, 5))
        assertEquals(500, EntropyTuning.decayStep(498, 500, 5))
        assertEquals(500, EntropyTuning.decayStep(500, 500, 5))
        assertEquals(0, EntropyTuning.decayStep(0, 0, 5))
    }

    @Test
    fun accumulatorKeepsTheFraction() {
        val a = ProgressAccumulator()
        assertEquals(listOf(1, 2, 1, 2), List(4) { a.step(1.5) })
        assertEquals(10, (1..10).sumOf { ProgressAccumulator().step(1.0) })
        val b = ProgressAccumulator()
        assertEquals(30, (1..20).sumOf { b.step(EntropyTuning.speed(EntropyLevel.CHARGED)) })
    }

    @Test
    fun bonusChance() {
        assertTrue(EntropyTuning.bonus(EntropyLevel.CHARGED, 0.0999))
        assertFalse(EntropyTuning.bonus(EntropyLevel.CHARGED, 0.1))
        assertTrue(EntropyTuning.bonus(EntropyLevel.CRITICAL, 0.0))
        assertFalse(EntropyTuning.bonus(EntropyLevel.STABLE, 0.0))
    }
}
