package io.github.srdjanv.endreforked.common.entropy.world

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk
import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import io.github.srdjanv.endreforked.common.ModBioms
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.blocks.BlockLormyteStabilizer
import io.github.srdjanv.endreforked.common.entropy.Stabilizers
import io.github.srdjanv.endreforked.common.entropy.CriticalEvents
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraft.world.chunk.Chunk
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

class EntropyWorld(private val world: World) {
    private companion object {
        const val MAX_TIER = BlockLormyteStabilizer.MAX_TIER
    }

    // one map per world, so dimensions never share entries; dropped with the world
    private val chunks = ConcurrentHashMap<Long, EntropyChunk>()

    // The Lormyte Stabilizers of the loaded chunks, by the chunk that holds the crystal. Filled by LormyteStabilizerTile.
    private val stabilizers = ConcurrentHashMap<Long, MutableSet<BlockPos>>()

    fun addStabilizer(pos: BlockPos) {
        stabilizers.computeIfAbsent(ChunkPos.asLong(pos.x shr 4, pos.z shr 4)) { ConcurrentHashMap.newKeySet() }.add(pos.toImmutable())
    }

    fun removeStabilizer(pos: BlockPos) {
        val key = ChunkPos.asLong(pos.x shr 4, pos.z shr 4)
        stabilizers[key]?.let { set ->
            set.remove(pos)
            if (set.isEmpty()) stabilizers.remove(key, set)
        }
    }

    /** The crystals that cover the chunk, on or off. A tier covers 0, 1 or 2 chunks around its own. */
    fun stabilizersCovering(chunkX: Int, chunkZ: Int): List<BlockPos> {
        val found = ArrayList<BlockPos>()
        for (dx in -MAX_TIER..MAX_TIER) for (dz in -MAX_TIER..MAX_TIER) {
            val crystals = stabilizers[ChunkPos.asLong(chunkX + dx, chunkZ + dz)] ?: continue
            for (pos in crystals) {
                if (!world.isBlockLoaded(pos)) continue
                val state = world.getBlockState(pos)
                if (state.block === ModBlocks.LORMYTE_STABILIZER && Stabilizers.covers(state.getValue(BlockLormyteStabilizer.TIER), dx, dz)) found += pos
            }
        }
        return found
    }

    /** True when a crystal that is on (not powered) covers the chunk, and the chunk is not in the Entropy biome. */
    fun isCapped(chunkX: Int, chunkZ: Int): Boolean {
        if (stabilizers.isEmpty() || inEntropyBiome(chunkX, chunkZ)) return false
        return stabilizersCovering(chunkX, chunkZ).any { !world.getBlockState(it).getValue(BlockLormyteStabilizer.POWERED) }
    }

    /** The biome of the middle of the chunk decides, not the rift. */
    fun inEntropyBiome(chunkX: Int, chunkZ: Int) = world.getBiome(BlockPos((chunkX shl 4) + 8, 0, (chunkZ shl 4) + 8)) === ModBioms.ENTROPY_BIOME

    fun getEntropyChunk(chunkX: Int, chunkZ: Int): Optional<EntropyChunk> =
        if (world.isAreaLoaded(BlockPos(chunkX shl 4, 0, chunkZ shl 4), 0)) {
            Optional.ofNullable(chunks[ChunkPos.asLong(chunkX, chunkZ)])
        } else Optional.empty()

    /** One decay step: every loaded chunk moves by its `decay` towards the base level of its biome. */
    fun decay() {
        for (chunk in chunks.values) {
            if (!chunk.isLoaded) continue
            val biome = world.getBiome(chunk.dimPos.asBlockPos().add(8, 0, 8))
            val base = if (biome === ModBioms.ENTROPY_BIOME) chunk.maxEntropy.toLong() * EntropyTuning.entropyBiomePercent / 100 else 0
            val delta = EntropyTuning.decayStep(chunk.currentEntropy, base.toInt(), chunk.decay) - chunk.currentEntropy
            if (delta == 0) continue
            if (delta > 0) chunk.induceEntropy(delta, false) else chunk.drainEntropy(-delta, false)
            world.getChunk(chunk.dimPos.chunkPosX, chunk.dimPos.chunkPosZ).markDirty() // the capability alone does not make the chunk save
        }
    }

    /**
     * Every loaded critical chunk rolls one event: the chance rises with the fill (see [EntropyTuning.criticalChance]).
     * The event happens at a random column of the chunk and costs entropy. A chunk with too little entropy skips it.
     */
    fun fireCriticalEvents() {
        val world = world as? WorldServer ?: return
        val events = EndReforked.getProxy().registries(world).criticalEvents.current.entries
        if (events.isEmpty()) return
        for (chunk in chunks.values) {
            if (!chunk.isLoaded || EntropyTuning.level(chunk.currentEntropy, chunk.maxEntropy) != EntropyLevel.CRITICAL) continue
            if (world.rand.nextDouble() >= EntropyTuning.criticalChance(chunk.currentEntropy, chunk.maxEntropy)) continue
            val event = CriticalEvents.pick(events, world.rand) ?: continue
            if (chunk.currentEntropy < event.entropyCost) continue
            val x = (chunk.dimPos.chunkPosX shl 4) + world.rand.nextInt(16)
            val z = (chunk.dimPos.chunkPosZ shl 4) + world.rand.nextInt(16)
            val pos = BlockPos(x, world.getHeight(x, z), z)
            chunk.drainEntropy(event.entropyCost, false)
            world.getChunk(chunk.dimPos.chunkPosX, chunk.dimPos.chunkPosZ).markDirty()
            // An absorbed event costs the entropy but does not happen.
            if (Stabilizers.absorb(world, pos)) continue
            CriticalEvents.run(world, event, pos)
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
