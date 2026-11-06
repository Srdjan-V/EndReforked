package io.github.srdjanv.endreforked.common.entropy.world

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class EntropyWorld(private val world: World) {
    fun getEntropyChunk(chunkX: Int, chunkZ: Int): Optional<EntropyChunk> =
        if (world.isAreaLoaded(BlockPos(chunkX shl 4, 0, chunkZ shl 4), 0)) {
            Optional.ofNullable(chunks[ChunkPos.asLong(chunkX, chunkZ)])
        } else Optional.empty()

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

    private companion object {
        // shared by every world, keyed by chunk position only
        val chunks = ConcurrentHashMap<Long, EntropyChunk>()
    }
}
