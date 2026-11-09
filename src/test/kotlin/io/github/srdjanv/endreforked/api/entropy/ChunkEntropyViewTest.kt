package io.github.srdjanv.endreforked.api.entropy

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage
import io.github.srdjanv.endreforked.api.entropy.world.ChunkEntropyView
import io.github.srdjanv.endreforked.api.util.DimPos
import net.minecraft.nbt.NBTTagCompound
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.function.Function
import java.util.function.Supplier

class ChunkEntropyViewTest {
    private class FakeChunk(override val dimPos: DimPos, override var currentEntropy: Int, override val maxEntropy: Int = 1000) : EntropyChunk {
        override val isLoaded = true
        override var loadFactor = 1.0
        override val isOverLoaded get() = false
        override var decay = 0
        override val entropyStorageReference: Optional<EntropyStorage> = Optional.empty()

        override fun setEntropyStorageReference(reference: EntropyStorage?, force: Boolean) = false

        override fun induceEntropy(entropy: Int, simulate: Boolean): Int {
            val moved = minOf(entropy, maxEntropy - currentEntropy)
            if (!simulate) currentEntropy += moved
            return moved
        }

        override fun drainEntropy(entropy: Int, simulate: Boolean): Int {
            val moved = minOf(entropy, currentEntropy)
            if (!simulate) currentEntropy -= moved
            return moved
        }

        override fun serializeNBT() = NBTTagCompound()

        override fun deserializeNBT(nbt: NBTTagCompound) {}
    }

    /** A 3x3 view whose chunks hold the given entropy, in order. */
    private fun view(vararg entropy: Int): Pair<ChunkEntropyView, Map<DimPos, FakeChunk>> {
        val center = DimPos(1, 0, 0)
        val chunks = EntropyRadius.TWO.getChunksInRadius(center).withIndex().associate { (i, p) -> p to FakeChunk(p, entropy.getOrElse(i) { 0 }) }
        val view = ChunkEntropyView(Supplier { center }, Function { Optional.ofNullable(chunks[it]) }, EntropyRadius.TWO)
        return view to chunks
    }

    @Test
    fun aDrainTakesFromTheFullestChunkFirst() {
        val (view, chunks) = view(50, 300, 900)
        assertEquals(100, view.drainEntropy(100, false))
        assertEquals(listOf(50, 300, 800), chunks.values.take(3).map { it.currentEntropy })
        assertEquals(1150, view.drainEntropy(10_000, false))
        assertEquals(0, view.currentEntropy)
    }

    @Test
    fun aDrainSpansChunksInOrderOfFill() {
        val (view, chunks) = view(50, 300, 900)
        assertEquals(1000, view.drainEntropy(1000, false))
        assertEquals(listOf(50, 200, 0), chunks.values.take(3).map { it.currentEntropy })
    }

    @Test
    fun aSimulatedDrainChangesNothing() {
        val (view, chunks) = view(50, 300, 900)
        assertEquals(1250, view.drainEntropy(5000, true))
        assertEquals(1250, chunks.values.sumOf { it.currentEntropy })
    }

    @Test
    fun anInduceGoesToTheEmptiestChunkFirst() {
        val (view, chunks) = view(50, 300, 900)
        assertEquals(1500, view.induceEntropy(1500, false))
        val all = chunks.values.toList()
        assertEquals(listOf(50, 300, 900), all.take(3).map { it.currentEntropy }) // the filled ones stay
        assertEquals(listOf(1000, 500), all.slice(3..4).map { it.currentEntropy })
    }
}
