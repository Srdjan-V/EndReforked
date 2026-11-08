package io.github.srdjanv.endreforked.utils

import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer

fun renderParticle(pos: BlockPos, world: World, particleType: EnumParticleTypes, particleColor: Int) {
    world.spawnParticle(
        particleType,
        pos.x + (world.rand.nextDouble() - 0.5),
        pos.y + (world.rand.nextDouble() - 0.5),
        pos.z + (world.rand.nextDouble() - 0.5),
        (particleColor shr 16 and 255) / 255.0,
        (particleColor shr 8 and 255) / 255.0,
        (particleColor and 255) / 255.0
    )
}

fun renderParticle(pos: BlockPos, world: WorldServer, particleType: EnumParticleTypes, particleColor: Int) {
    world.spawnParticle(
        particleType,
        pos.x + (world.rand.nextDouble() - 0.5),
        pos.y + (world.rand.nextDouble() - 0.5),
        pos.z + (world.rand.nextDouble() - 0.5),
        3,
        (particleColor shr 16 and 255) / 255.0,
        (particleColor shr 8 and 255) / 255.0,
        (particleColor and 255) / 255.0,
        0.0,
        1
    )
}

