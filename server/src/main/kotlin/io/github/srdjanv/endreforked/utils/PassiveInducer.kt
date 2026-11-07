package io.github.srdjanv.endreforked.utils

import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer

fun randomTickPassiveInducer(
    world: World,
    pos: BlockPos,
    tuning: EntropyTuning.Inducer,
    particleColor: Int,
    particleType: EnumParticleTypes,
) {
    if (world.isRemote) return
    val entropyView = EntropyWorldHandler.getEntropyChunkWorld(world.provider.dimension, pos)
    if (entropyView.isPresent && entropyView.get().induceEntropy(tuning.amount, false) > 0) {
        renderParticle(pos, world as WorldServer, particleType, particleColor)
    }
}
