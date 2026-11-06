package io.github.srdjanv.endreforked.common.entropy.world

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk
import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import io.github.srdjanv.endreforked.api.util.DimPos
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

object EntropyWorldHandler {
    private val worlds = ConcurrentHashMap<Int, EntropyWorld>()

    fun getEntropyChunkWorld(pos: DimPos): Optional<EntropyChunk> =
        getEntropyWorld(pos.dim)?.getEntropyChunk(pos.chunkPosX, pos.chunkPosZ) ?: Optional.empty()

    fun getEntropyChunkWorld(dim: Int, pos: BlockPos): Optional<EntropyChunk> =
        getEntropyWorld(dim)?.getEntropyChunk(pos.x shr 4, pos.z shr 4) ?: Optional.empty()

    /** The level of the chunk at [pos]. Stable if the chunk has no entropy data. */
    fun levelAt(dim: Int, pos: BlockPos): EntropyLevel =
        getEntropyChunkWorld(dim, pos).map { EntropyTuning.level(it.currentEntropy, it.maxEntropy) }.orElse(EntropyLevel.STABLE)

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
    fun onWorldTick(event: TickEvent.WorldTickEvent) {
        val world = event.world
        if (event.phase != TickEvent.Phase.END || world.isRemote || world.totalWorldTime % EntropyTuning.DECAY_INTERVAL != 0L) return
        getEntropyWorld(world.provider.dimension)?.decay()
    }

    @SubscribeEvent
    @JvmStatic
    fun onChunkLoad(event: ChunkEvent.Load) {
        if (event.world.isRemote) return
        val dim = event.world.provider.dimension
        val world = getEntropyWorld(dim) ?: run {
            EndReforked.LOGGER.warn("Chunk was loaded before world was loaded????")
            worlds.computeIfAbsent(dim) { EntropyWorld(event.world) }
        }
        world.loadData(event.chunk)
    }

    @SubscribeEvent
    @JvmStatic
    fun onChunkUnLoad(event: ChunkEvent.Unload) {
        if (event.world.isRemote) return
        val dim = event.world.provider.dimension
        // no world means its chunk map is already gone
        getEntropyWorld(dim)?.unloadData(event.chunk)
    }
}
