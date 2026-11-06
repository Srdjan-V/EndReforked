package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.common.network.Network
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import io.github.srdjanv.endreforked.wire.s2c.S2CPacket
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import org.apache.logging.log4j.Logger

/**
 * The server side of the registries: builds them once and sends the snapshots to the clients.
 * A snapshot goes to a player on login, and to all players whenever a registry publishes a new one while the server runs.
 * So a script reload that calls `rebuild()` needs no command of ours.
 * FML splits a payload above 1 MiB into parts itself, so a snapshot needs no chunking.
 */
class RegistrySync(val registries: Registries, private val net: Network, private val logger: Logger) {
    /** Applies the defaults and the script layer of every registry, logs the counts, and starts the sync on later rebuilds. Call it once in init. */
    fun build() {
        for ((slot, report) in registries.rebuildAll()) {
            logger.info("Built {} {}", report.total, slot.name)
            report.errors.forEach { logger.error("{}: {}", slot.name, it) }
        }
        registries.slots.forEach { slot -> slot.onSnapshot?.invoke { sendToAll(slot.name, it) } }
    }

    private fun sendToAll(name: String, snapshot: RegistrySnapshot) {
        val server = FMLCommonHandler.instance().minecraftServerInstance ?: return
        // The rebuild can run on any thread, the channel is used on the server thread.
        server.addScheduledTask {
            net.sendToAll(S2CPacket(registry_snapshot = snapshot))
            logger.info("Synced {} to {} players", name, server.playerList.currentPlayerCount)
        }
    }

    @SubscribeEvent
    fun onLogin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.player as? EntityPlayerMP ?: return
        val snapshots = registries.snapshots()
        snapshots.forEach { net.sendTo(S2CPacket(registry_snapshot = it), player) }
        logger.info("Sent {} registry snapshots to {}", snapshots.size, player.name)
    }
}
