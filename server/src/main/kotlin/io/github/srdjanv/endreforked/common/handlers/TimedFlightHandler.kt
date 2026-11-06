package io.github.srdjanv.endreforked.common.handlers

import io.github.srdjanv.endreforked.api.entropy.EntropyWings
import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import io.github.srdjanv.endreforked.utils.Initializer
import io.github.srdjanv.endreforked.utils.PlayerUtils
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object TimedFlightHandler : Initializer {
    override fun registerEventBus() {
        registerThisToEventBus()
    }

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase == TickEvent.Phase.END) return

        for (player in FMLCommonHandler.instance().minecraftServerInstance.playerList.players) {
            val cap = player.getCapability(CapabilityTimedFlightHandler.INSTANCE, null) ?: continue
            cap.tickPlayer(player)
            if (cap.flightDuration > 0) continue
            for (stack in PlayerUtils.getAllPlayerItems(player)) {
                val wings = stack.item as? EntropyWings ?: continue
                if (stack.isEmpty) continue
                val data = EntropyChunkReader.ofEntity(player, wings.entropyRadius).entropyView
                if (data.drainEntropy(wings.entropyCost, true) == wings.entropyCost) {
                    data.drainEntropy(wings.entropyCost, false)
                    cap.flightDuration = wings.flightDuration
                }
            }
        }
    }
}
