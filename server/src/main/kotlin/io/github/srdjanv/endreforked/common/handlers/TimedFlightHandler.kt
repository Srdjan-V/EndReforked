package io.github.srdjanv.endreforked.common.handlers

import io.github.srdjanv.endreforked.api.entropy.EntropyWings
import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler
import io.github.srdjanv.endreforked.common.entropy.EntropyCell
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import io.github.srdjanv.endreforked.common.items.ItemEntropyCell
import net.minecraft.entity.Entity
import io.github.srdjanv.endreforked.utils.Initializer
import io.github.srdjanv.endreforked.utils.PlayerUtils
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object TimedFlightHandler : Initializer {
    override fun registerEventBus() {
        registerThisToEventBus()
    }

    /** [base] ticks of flight, scaled by the entropy level of the chunk of the [player]. */
    fun scaled(player: Entity, base: Int) =
        EntropyTuning.flightDuration(base, EntropyWorldHandler.levelAt(player.world.provider.dimension, player.position))

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        if (event.phase == TickEvent.Phase.END) return

        for (player in FMLCommonHandler.instance().minecraftServerInstance.playerList.players) {
            val cap = player.getCapability(CapabilityTimedFlightHandler.CAPABILITY, null) ?: continue
            cap.tickPlayer(player)
            if (cap.flightDuration > 0) continue
            for (stack in PlayerUtils.getAllPlayerItems(player)) {
                val wings = stack.item as? EntropyWings ?: continue
                if (stack.isEmpty) continue
                val data = EntropyChunkReader.ofEntity(player, wings.entropyRadius).entropyView
                if (data.drainEntropy(wings.entropyCost, true) == wings.entropyCost) {
                    data.drainEntropy(wings.entropyCost, false)
                    cap.flightDuration = scaled(player, wings.flightDuration)
                }
            }
            if (cap.isExpiring && !player.capabilities.isCreativeMode && (player.capabilities.isFlying || !player.onGround)) {
                EntropyCell.extendFlight(cap, EntropyTuning.flightCellCost, EntropyTuning.flightCellExtension) { cost ->
                    PlayerUtils.getAllPlayerItems(player).any { ItemEntropyCell.take(it, cost) }
                }
            }
        }
    }
}
