package io.github.srdjanv.endreforked.common.entropy.world

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk
import io.github.srdjanv.endreforked.common.ModBioms
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class EntropyWorld(private val world: World) {
    // one map per world, so dimensions never share entries; dropped with the world
    private val chunks = ConcurrentHashMap<Long, EntropyChunk>()

    fun getEntropyChunk(chunkX: Int, chunkZ: Int): Optional<EntropyChunk> =
        if (world.isAreaLoaded(BlockPos(chunkX shl 4, 0, chunkZ shl 4), 0)) {
            Optional.ofNullable(chunks[ChunkPos.asLong(chunkX, chunkZ)])
        } else Optional.empty()

    /** One decay step: every loaded chunk moves by its `decay` towards the base level of its biome. */
    fun decay() {
        for (chunk in chunks.values) {
            if (!chunk.isLoaded) continue
            val biome = world.getBiome(chunk.dimPos.asBlockPos().add(8, 0, 8))
            val base = if (biome === ModBioms.ENTROPY_BIOME.get()) chunk.maxEntropy.toLong() * EntropyTuning.entropyBiomePercent / 100 else 0
            val delta = EntropyTuning.decayStep(chunk.currentEntropy, base.toInt(), chunk.decay) - chunk.currentEntropy
            if (delta == 0) continue
            if (delta > 0) chunk.induceEntropy(delta, false) else chunk.drainEntropy(-delta, false)
            world.getChunk(chunk.dimPos.chunkPosX, chunk.dimPos.chunkPosZ).markDirty() // the capability alone does not make the chunk save
        }
    }

    fun loadData(chunk: Chunk) {
        val data = chunk.getCapability(CapabilityEntropyHandler.ENTROPY_CHUNK, null)
        if (data == null) {
            EndReforked.LOGGER.error("Chunk entropy capability not found {}", chunk.pos)
            return
        }
        if (chunks.put(ChunkPos.asLong(chunk.x, chunk.z), data) != null) {
            EndReforked.LOGGER.error("Chunk entropy capability already loaded {}", chunk.pos)
        }
    }

    fun unloadData(chunk: Chunk) {
        chunks.remove(ChunkPos.asLong(chunk.x, chunk.z))
    }
}
