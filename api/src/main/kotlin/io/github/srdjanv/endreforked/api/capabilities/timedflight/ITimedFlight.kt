package io.github.srdjanv.endreforked.api.capabilities.timedflight

import net.minecraft.entity.player.EntityPlayerMP

interface ITimedFlight {
    var flightDuration: Int

    fun tickPlayer(playerMP: EntityPlayerMP)
}
