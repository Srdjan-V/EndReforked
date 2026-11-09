package io.github.srdjanv.endreforked.client

import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import io.github.srdjanv.endreforked.wire.s2c.ChunkEntropy
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

private const val INTERVAL = 4 // ticks between two bursts
private const val CHARGED_PARTICLES = 1 // per chunk and burst
private const val CRITICAL_PARTICLES = 4
private const val CLOUD_HEIGHT = 4 // random height above the surface

/** Purple particle clouds over the nearby chunks that are charged or critical. Every player sees them. */
class EntropyClouds(private val entropy: () -> ChunkEntropy?) {
    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        val mc = Minecraft.getMinecraft()
        val world = mc.world ?: return
        if (event.phase != TickEvent.Phase.END || mc.player == null || mc.isGamePaused || world.totalWorldTime % INTERVAL != 0L) return
        val data = entropy() ?: return
        val rng = world.rand
        for ((i, ordinal) in data.nearby_levels.withIndex()) {
            val level = EntropyLevel.values().getOrElse(ordinal) { EntropyLevel.STABLE }
            if (level < EntropyLevel.CHARGED) continue
            val originX = (data.chunk_x + i % 5 - 2) shl 4
            val originZ = (data.chunk_z + i / 5 - 2) shl 4
            if (!world.isBlockLoaded(BlockPos(originX, 0, originZ))) continue
            repeat(if (level == EntropyLevel.CRITICAL) CRITICAL_PARTICLES else CHARGED_PARTICLES) {
                spawn(EnumParticleTypes.SPELL_MOB, originX, originZ, 0.35, 0.0, 0.7)
            }
            if (level == EntropyLevel.CRITICAL) {
                spawn(EnumParticleTypes.DRAGON_BREATH, originX, originZ, rng.nextGaussian() * 0.02, 0.01, rng.nextGaussian() * 0.02)
            }
        }
    }

    /** Spawns [type] at a random spot above the surface of the chunk at [originX], [originZ]. */
    private fun spawn(type: EnumParticleTypes, originX: Int, originZ: Int, vx: Double, vy: Double, vz: Double) {
        val world = Minecraft.getMinecraft().world
        val x = originX + world.rand.nextInt(16)
        val z = originZ + world.rand.nextInt(16)
        val y = world.getHeight(x, z) + world.rand.nextDouble() * CLOUD_HEIGHT
        world.spawnParticle(type, x + 0.5, y, z + 0.5, vx, vy, vz)
    }
}
