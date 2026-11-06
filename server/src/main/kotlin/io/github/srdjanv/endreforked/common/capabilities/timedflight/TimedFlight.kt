package io.github.srdjanv.endreforked.common.capabilities.timedflight

import io.github.srdjanv.endreforked.api.capabilities.timedflight.ITimedFlight
import io.github.srdjanv.endreforked.common.ModPotions
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.potion.PotionEffect

class TimedFlight : ITimedFlight {
    private var sendStartUpdate = false
    private var sendStopUpdate = false
    private var duration = 0

    override var flightDuration: Int
        get() = duration
        set(value) {
            if (value <= 0) return
            duration = value
            sendStopUpdate = true
            sendStartUpdate = true
        }

    override val isExpiring get() = duration <= 0 && sendStopUpdate

    override fun tickPlayer(playerMP: EntityPlayerMP) {
        if (duration > 0) {
            if (sendStartUpdate) {
                sendStartUpdate = false
                sendUpdate(playerMP, true)
            }
            duration--
        } else if (sendStopUpdate) {
            sendStopUpdate = false
            sendUpdate(playerMP, false)
        }
    }

    private fun sendUpdate(playerMP: EntityPlayerMP, flight: Boolean) {
        if (playerMP.capabilities.isCreativeMode) return
        val potion = ModPotions.TIMED_FLIGHT.get()
        if (flight) playerMP.addPotionEffect(PotionEffect(potion, duration)) else playerMP.removePotionEffect(potion)
        playerMP.capabilities.allowFlying = flight
        if (!flight) playerMP.capabilities.isFlying = false
        playerMP.sendPlayerAbilities()
    }
}
