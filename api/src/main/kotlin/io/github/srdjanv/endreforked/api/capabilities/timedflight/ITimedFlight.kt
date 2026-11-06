package io.github.srdjanv.endreforked.api.capabilities.timedflight

import net.minecraft.entity.player.EntityPlayerMP

interface ITimedFlight {
    var flightDuration: Int

    /** True when the flight ran out and the stop update is not sent yet. */
    val isExpiring: Boolean

    fun tickPlayer(playerMP: EntityPlayerMP)
}
