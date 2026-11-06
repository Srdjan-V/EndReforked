package io.github.srdjanv.endreforked.common.entropy

import io.github.srdjanv.endreforked.api.entropy.CriticalAction
import io.github.srdjanv.endreforked.api.entropy.CriticalEvent
import net.minecraft.util.ResourceLocation
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CriticalEventsTest {
    @AfterEach fun reset() = EntropyTuning.reset()

    private fun event(name: String, weight: Int) = CriticalEvent(ResourceLocation("t", name), weight, 1, 1, CriticalAction.Convert)

    @Test
    fun weightedPick() {
        val list = listOf(event("a", 10), event("b", 5), event("c", 2))
        val picked = (0 until 17).map { CriticalEvents.pick(list, it)!!.id.path }
        assertEquals(List(10) { "a" } + List(5) { "b" } + List(2) { "c" }, picked)
        assertNull(CriticalEvents.pick(list, 17))
        assertNull(CriticalEvents.pick(emptyList(), 0))
    }

    @Test
    fun chanceRisesFrom50To100Percent() {
        assertEquals(0.5, EntropyTuning.criticalChance(800, 1000), 1e-9)
        assertEquals(0.75, EntropyTuning.criticalChance(900, 1000), 1e-9)
        assertEquals(1.0, EntropyTuning.criticalChance(1000, 1000), 1e-9)
        EntropyTuning.criticalBaseChance = 20
        assertEquals(0.2, EntropyTuning.criticalChance(800, 1000), 1e-9)
        assertEquals(0.0, EntropyTuning.criticalChance(5, 0), 1e-9)
    }

    @Test
    fun protectionOnlyCountsAnchoredPlayersInRange() {
        val players = listOf(10, 100) // the number is the squared distance
        val anchored = listOf<(Int) -> Boolean> { it == 100 }
        assertFalse(CriticalEvents.isProtected(players, emptyList()) { it.toDouble() })
        assertFalse(CriticalEvents.isProtected(players, anchored) { it.toDouble() }) // the anchored one is too far (100 > 64)
        assertTrue(CriticalEvents.isProtected(players, listOf<(Int) -> Boolean> { it == 10 }) { it.toDouble() })
    }
}
