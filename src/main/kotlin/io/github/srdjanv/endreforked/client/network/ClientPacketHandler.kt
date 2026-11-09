package io.github.srdjanv.endreforked.client.network

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.client.ParticleHandler
import io.github.srdjanv.endreforked.common.network.Network
import io.github.srdjanv.endreforked.common.registry.Registries
import io.github.srdjanv.endreforked.impl.machine.toMachineRecipes
import io.github.srdjanv.endreforked.impl.world.toFluidBlockInteractions
import io.github.srdjanv.endreforked.impl.world.toFluidEntityInteractions
import io.github.srdjanv.endreforked.wire.s2c.ChunkEntropy
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import io.github.srdjanv.endreforked.wire.s2c.PlayerParticle
import io.github.srdjanv.endreforked.wire.s2c.S2CPacket
import io.github.srdjanv.endreforked.wire.s2c.TpParticle
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

/** Receives the S2CPacket envelope and dispatches on the oneof field that is set. */
class ClientPacketHandler(
    network: Network,
    private val registries: Registries,
    private val onChunkEntropy: (ChunkEntropy?) -> Unit,
) {
    init {
        network.channel.register(this)
        MinecraftForge.EVENT_BUS.register(this)
    }

    /** Back to the code defaults when the player leaves a server, so the menu and JEI do not show that server's recipes. */
    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        Minecraft.getMinecraft().addScheduledTask { registries.rebuildAll(); onChunkEntropy(null) }
    }

    @SubscribeEvent
    fun onPacket(event: FMLNetworkEvent.ClientCustomPacketEvent) {
        val buf = event.packet.payload()
        val bytes = ByteArray(buf.readableBytes())
        buf.readBytes(bytes)
        val packet = S2CPacket.ADAPTER.decode(bytes)
        // The event runs on the network thread
        Minecraft.getMinecraft().addScheduledTask {
            packet.player_particle?.let(::playerParticle)
            packet.tp_particle?.let(::tpParticle)
            packet.registry_snapshot?.let(::registrySnapshot)
            packet.chunk_entropy?.let(onChunkEntropy)
        }
    }

    private fun registrySnapshot(snapshot: RegistrySnapshot) {
        try {
            val known = snapshot.toMachineRecipes()?.let(registries.machineRecipes::replace)
                ?: snapshot.toFluidBlockInteractions()?.let(registries.fluidBlockInteractions::replace)
                ?: snapshot.toFluidEntityInteractions()?.let(registries.fluidEntityInteractions::replace)
            if (known == null) EndReforked.LOGGER.warn("Unknown registry snapshot {}", snapshot.registry)
            else EndReforked.LOGGER.info("Received registry snapshot {}", snapshot.registry)
        } catch (e: Exception) {
            EndReforked.LOGGER.error("Bad registry snapshot {}", snapshot.registry, e)
        }
    }

    private fun playerParticle(message: PlayerParticle) {
        if (message.remove) {
            ParticleHandler.removeFromRender(message.id)
            return
        }
        val particle = EnumParticleTypes.getParticleFromId(message.particle_type)
        ParticleHandler.addToRender(object : ParticleHandler.RenderInfo(message.id, message.render_iterations) {
            override fun render() {
                val color = 0x5900b3
                val xSpeed = (color shr 16 and 255) / 255.0
                val ySpeed = (color shr 8 and 255) / 255.0
                val zSpeed = (color and 255) / 255.0

                val mc = Minecraft.getMinecraft()
                val player = mc.player
                mc.world.spawnParticle(
                    particle,
                    player.posX + (player.rng.nextDouble() - 0.5) * player.width,
                    player.posY + player.rng.nextDouble() * player.height,
                    player.posZ + (player.rng.nextDouble() - 0.5) * player.width,
                    xSpeed, ySpeed, zSpeed,
                )
            }
        })
    }

    private fun tpParticle(m: TpParticle) {
        ParticleHandler.addToRender(object : ParticleHandler.RenderInfo("TPParticlePacket" + Minecraft.getSystemTime(), 1) {
            override fun render() {
                val mc = Minecraft.getMinecraft()
                val random = mc.world.rand
                for (j in 0 until 128) {
                    val d6 = j / 127.0
                    val f = (random.nextFloat() - 0.5f) * 0.2f
                    val f1 = (random.nextFloat() - 0.5f) * 0.2f
                    val f2 = (random.nextFloat() - 0.5f) * 0.2f
                    val d3 = m.pos_x + (m.new_pos_x - m.pos_x) * d6 + (random.nextDouble() - 0.5) * m.width * 2.0
                    val d4 = m.pos_y + (m.new_pos_y - m.pos_y) * d6 + random.nextDouble() * m.height
                    val d5 = m.pos_z + (m.new_pos_z - m.pos_z) * d6 + (random.nextDouble() - 0.5) * m.width * 2.0
                    mc.world.spawnParticle(EnumParticleTypes.PORTAL, d3, d4, d5, f.toDouble(), f1.toDouble(), f2.toDouble())
                }
            }
        })
    }
}
