package io.github.srdjanv.endreforked.common.entropy.world

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk
import io.github.srdjanv.endreforked.api.util.DimPos
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

object EntropyWorldHandler {
    private val worlds = ConcurrentHashMap<Int, EntropyWorld>()

    fun getEntropyChunkWorld(pos: DimPos): Optional<EntropyChunk> =
        getEntropyWorld(pos.dim)?.getEntropyChunk(pos.chunkPosX, pos.chunkPosZ) ?: Optional.empty()

    fun getEntropyChunkWorld(dim: Int, pos: BlockPos): Optional<EntropyChunk> =
        getEntropyWorld(dim)?.getEntropyChunk(pos.x shr 4, pos.z shr 4) ?: Optional.empty()

    private fun getEntropyWorld(dim: Int): EntropyWorld? {
        val entropyWorld = worlds[dim] ?: return null
        if (!DimensionManager.isWorldQueuedToUnload(dim)) return entropyWorld
        worlds.remove(dim)
        return null
    }

    @SubscribeEvent
    @JvmStatic
    fun onWorldLoad(event: WorldEvent.Load) {
        if (!event.world.isRemote) worlds[event.world.provider.dimension] = EntropyWorld(event.world)
    }

    @SubscribeEvent
    @JvmStatic
    fun onWorldUnLoad(event: WorldEvent.Unload) {
        worlds.remove(event.world.provider.dimension)
    }

    @SubscribeEvent
    @JvmStatic
    fun onChunkLoad(event: ChunkEvent.Load) {
        if (event.world.isRemote) return
        val dim = event.world.provider.dimension
        var world = getEntropyWorld(dim)
        if (world == null) {
            EndReforked.LOGGER.warn("Chunk was loaded before world was loaded????")
            world = worlds.put(dim, EntropyWorld(event.world))
        }
        if (world != null) world.loadData(event.chunk)
        else EndReforked.LOGGER.error("Cant load chunk data {}", event.chunk.pos)
    }

    @SubscribeEvent
    @JvmStatic
    fun onChunkUnLoad(event: ChunkEvent.Unload) {
        if (event.world.isRemote) return
        val dim = event.world.provider.dimension
        var world = getEntropyWorld(dim)
        if (world == null) {
            EndReforked.LOGGER.warn("Chunk was unloaded after world was unloaded????")
            world = worlds.put(dim, EntropyWorld(event.world))
        }
        if (world != null) world.unloadData(event.chunk)
        else EndReforked.LOGGER.error("Cant unload chunk data {}", event.chunk.pos)
    }
}
