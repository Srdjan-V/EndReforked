package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.common.potions.EnderEyes
import io.github.srdjanv.endreforked.common.potions.TimedFlight
import io.github.srdjanv.endreforked.common.potions.base.BasePotion
import net.minecraft.potion.Potion
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ModPotions {
    private val POTIONS = Registry<BasePotion>()

    @JvmField val ENDER_EYES: BasePotion = register(::EnderEyes)
    @JvmField val TIMED_FLIGHT: BasePotion = register(::TimedFlight)

    private fun <I : BasePotion> register(factory: () -> I): I =
        factory().also { POTIONS.register(it) }

    @SubscribeEvent
    @JvmStatic
    fun onPotionRegister(event: RegistryEvent.Register<Potion>) {
        POTIONS.all().forEach(event.registry::register)
    }
}
