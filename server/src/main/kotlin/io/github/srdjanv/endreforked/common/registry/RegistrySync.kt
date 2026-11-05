package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.network.Network
import io.github.srdjanv.endreforked.wire.s2c.S2CPacket
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.relauncher.FMLInjectionData
import org.apache.logging.log4j.Logger
import java.io.File

/**
 * The server side of the registries: reads the JSON layer and sends the snapshots to the clients.
 * Snapshots go to a player on login and to all players after every reload.
 * FML splits a payload above 1 MiB into parts itself, so a snapshot needs no chunking.
 */
class RegistrySync(val registries: Registries, private val net: Network, private val logger: Logger) {
    private val machineRecipesDir get() = File((FMLInjectionData.data()[6] as File), "config/${Tags.MODID}/machine_recipes").toPath()

    /** The integrated or dedicated server is about to start. Reads the JSON files. There are no players yet. */
    fun onServerAboutToStart() {
        reload()
    }

    /** Reads the JSON layer again and returns the problems, one line each. Nothing is sent. */
    fun reload(): List<String> {
        val report = registries.machineRecipes.reload(machineRecipesDir)
        logger.info("Loaded {} machine recipes ({} from JSON)", report.total, report.applied["json"])
        report.errors.forEach { logger.error("Machine recipes: {}", it) }
        return report.errors
    }

    /** [reload], then sends the new content to all players. The command calls it. */
    fun reloadAndSync(): List<String> {
        val errors = reload()
        registries.snapshots().forEach { net.sendToAll(S2CPacket(registry_snapshot = it)) }
        return errors
    }

    @SubscribeEvent
    fun onLogin(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.player as? EntityPlayerMP ?: return
        registries.snapshots().forEach { net.sendTo(S2CPacket(registry_snapshot = it), player) }
    }
}
