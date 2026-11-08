package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.common.potions.EnderEyes
import io.github.srdjanv.endreforked.common.potions.TimedFlight
import net.minecraft.potion.Potion
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ModPotions {
    private val POTIONS = DeferredRegistry(this, Potion::class.java)

    @get:JvmStatic val ENDER_EYES by POTIONS.register(::EnderEyes)
    @get:JvmStatic val TIMED_FLIGHT by POTIONS.register(::TimedFlight)

    @SubscribeEvent
    @JvmStatic
    fun onPotionRegister(event: RegistryEvent.Register<Potion>) = POTIONS.registerAll(event.registry)
}
