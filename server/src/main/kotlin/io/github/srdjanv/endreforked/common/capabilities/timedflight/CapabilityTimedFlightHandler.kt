package io.github.srdjanv.endreforked.common.capabilities.timedflight

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.capabilities.timedflight.ITimedFlight
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagInt
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object CapabilityTimedFlightHandler {
    // Forge injects this static field
    @CapabilityInject(ITimedFlight::class)
    lateinit var CAPABILITY: Capability<ITimedFlight>

    @JvmField
    val TimedFlight = ResourceLocation(Tags.MODID, "TimedFlight")

    @SubscribeEvent
    @JvmStatic
    fun attachTimedFlightCap(event: AttachCapabilitiesEvent<Entity>) {
        if (event.`object` is EntityPlayerMP) event.addCapability(TimedFlight, TimedFlightCapabilityProvider())
    }

    fun register() {
        CapabilityManager.INSTANCE.register(ITimedFlight::class.java, object : Capability.IStorage<ITimedFlight> {
            override fun writeNBT(capability: Capability<ITimedFlight>, instance: ITimedFlight, side: EnumFacing?): NBTBase =
                NBTTagInt(instance.flightDuration)

            override fun readNBT(capability: Capability<ITimedFlight>, instance: ITimedFlight, side: EnumFacing?, nbt: NBTBase) {
                instance.flightDuration = (nbt as NBTTagInt).int
            }
        }, ::TimedFlight)
    }
}
