package io.github.srdjanv.endreforked.common.entropy

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import io.github.srdjanv.endreforked.wire.s2c.ChunkEntropy
import io.github.srdjanv.endreforked.wire.s2c.S2CPacket
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.UUID

/** Sends the entropy of the chunk of each player once a second, but only when it differs from the last message to that player. */
object ChunkEntropySync {
    private val last = HashMap<UUID, ChunkEntropy>()

    @SubscribeEvent
    @JvmStatic
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        val server = FMLCommonHandler.instance().minecraftServerInstance
        if (event.phase != TickEvent.Phase.END || server.tickCounter % 20 != 0) return
        for (player in server.playerList.players) {
            val chunk = EntropyWorldHandler.getEntropyChunkWorld(player.dimension, player.position).orElse(null) ?: continue
            val level = EntropyTuning.level(chunk.currentEntropy, chunk.maxEntropy)
            val cx = player.position.x shr 4
            val cz = player.position.z shr 4
            // levelAt only reads loaded chunks, the rest is stable
            val nearby = (-2..2).flatMap { dz ->
                (-2..2).map { dx -> EntropyWorldHandler.levelAt(player.dimension, BlockPos((cx + dx) shl 4, 0, (cz + dz) shl 4)).ordinal }
            }
            val message = ChunkEntropy(chunk.currentEntropy, chunk.maxEntropy, level.ordinal, cx, cz, nearby)
            if (last.put(player.uniqueID, message) != message) {
                EndReforked.NET.sendTo(S2CPacket(chunk_entropy = message), player)
            }
        }
    }

    @SubscribeEvent
    @JvmStatic
    fun onLogout(event: PlayerEvent.PlayerLoggedOutEvent) {
        last.remove(event.player.uniqueID)
    }

}
