package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.common.sounds.EndSound
import net.minecraft.util.SoundEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ModSounds {
    @JvmField val THE_VOID = EndSound("the_void")

    @SubscribeEvent
    @JvmStatic
    fun registerSounds(event: RegistryEvent.Register<SoundEvent>) {
        event.registry.register(THE_VOID)
    }
}
