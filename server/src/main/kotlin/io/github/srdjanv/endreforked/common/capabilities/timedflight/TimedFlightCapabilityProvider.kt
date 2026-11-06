package io.github.srdjanv.endreforked.common.capabilities.timedflight

import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler.CAPABILITY
import net.minecraft.nbt.NBTTagInt
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable

class TimedFlightCapabilityProvider : ICapabilitySerializable<NBTTagInt> {
    private val timedFlight = CAPABILITY.defaultInstance!!

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) = capability === CAPABILITY

    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? =
        if (capability === CAPABILITY) CAPABILITY.cast(timedFlight) else null

    override fun serializeNBT() = CAPABILITY.writeNBT(timedFlight, null) as NBTTagInt

    override fun deserializeNBT(nbt: NBTTagInt) = CAPABILITY.readNBT(timedFlight, null, nbt)
}
