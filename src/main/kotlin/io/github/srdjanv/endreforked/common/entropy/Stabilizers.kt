package io.github.srdjanv.endreforked.common.entropy

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.blocks.BlockLormyteStabilizer
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import net.minecraftforge.fluids.BlockFluidBase
import kotlin.math.abs
import kotlin.math.max

/** The rules of the Lormyte Stabilizer. The cap is in `DefaultEntropyChunk.induceEntropy`, the registry of the crystals is in `EntropyWorld`. */
object Stabilizers {
    /** True when a stabilizer of [tier] covers a chunk that is [dx] and [dz] chunks away from its own: tier 0 its chunk, 1 is 3x3, 2 is 5x5. */
    fun covers(tier: Int, dx: Int, dz: Int) = max(abs(dx), abs(dz)) <= tier

    /** How much entropy a chunk with [current] may still take when its cap is [cap]. Never negative. */
    fun room(current: Int, cap: Int) = (cap - current).coerceAtLeast(0)

    /**
     * Organa absorption: a powered-off stabilizer that covers the chunk of [pos] and has an Organa fluid source next to it
     * takes the damage. The fluid source is used up. Returns true when the effect at [pos] must not happen.
     * It does nothing in the Entropy biome (and when [EntropyTuning.absorptionEnabled] is off).
     */
    @JvmStatic
    fun absorb(world: WorldServer, pos: BlockPos): Boolean {
        if (!EntropyTuning.absorptionEnabled) return false
        val entropyWorld = EntropyWorldHandler.entropyWorld(world) ?: return false
        val chunkX = pos.x shr 4
        val chunkZ = pos.z shr 4
        if (entropyWorld.inEntropyBiome(chunkX, chunkZ)) return false
        for (crystal in entropyWorld.stabilizersCovering(chunkX, chunkZ)) {
            if (world.getBlockState(crystal).getValue(BlockLormyteStabilizer.POWERED).not()) continue
            for (side in EnumFacing.VALUES) {
                val fluidPos = crystal.offset(side)
                val fluid = world.getBlockState(fluidPos)
                if (fluid.block !== ModBlocks.FLUID_ORGANA_BLOCK || fluid.getValue(BlockFluidBase.LEVEL) != 0) continue
                world.setBlockToAir(fluidPos)
                world.spawnParticle(EnumParticleTypes.PORTAL, crystal.x + 0.5, crystal.y + 1.0, crystal.z + 0.5, 20, 0.3, 0.3, 0.3, 0.5)
                return true
            }
        }
        return false
    }
}
