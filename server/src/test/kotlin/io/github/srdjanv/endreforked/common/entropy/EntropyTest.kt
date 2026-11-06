package io.github.srdjanv.endreforked.common.entropy

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.api.util.Ticker
import io.github.srdjanv.endreforked.common.entropy.chunks.PassiveEntropyChunkDrainer
import io.github.srdjanv.endreforked.common.entropy.storage.DefaultEntropyStorage
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EntropyTest {
    @Test
    fun induceClampsToSpaceAndReturnsMovedAmount() {
        val s = DefaultEntropyStorage(100).apply { currentEntropy = 90 }
        assertEquals(10, s.induceEntropy(50, true))
        assertEquals(90, s.currentEntropy)
        assertEquals(10, s.induceEntropy(50, false))
        assertEquals(100, s.currentEntropy)
        assertEquals(0, s.induceEntropy(5, false))
        // small request into a storage that holds more than it has space for
        val t = DefaultEntropyStorage(100).apply { currentEntropy = 80 }
        assertEquals(5, t.induceEntropy(5, false))
        assertEquals(85, t.currentEntropy)
    }

    @Test
    fun drainClampsToStoredAmount() {
        val s = DefaultEntropyStorage(100).apply { currentEntropy = 30 }
        assertEquals(30, s.drainEntropy(50, true))
        assertEquals(30, s.currentEntropy)
        assertEquals(30, s.drainEntropy(50, false))
        assertEquals(0, s.currentEntropy)
        assertEquals(0, s.drainEntropy(-5, false))
    }

    @Test
    fun drainerUsesChunkCount() {
        val reader = EntropyChunkReader({ null })
        for (r in EntropyRadius.entries)
            assertEquals(150 * r.chunks, PassiveEntropyChunkDrainer(reader, Ticker(200), { 150 }) { r }.drained)
        assertEquals(150, PassiveEntropyChunkDrainer(reader, Ticker(200), { 150 }) { EntropyRadius.ONE }.drained)
    }
}
