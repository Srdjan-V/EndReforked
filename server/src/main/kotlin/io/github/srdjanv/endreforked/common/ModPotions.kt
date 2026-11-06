package io.github.srdjanv.endreforked.common

import com.google.common.base.Suppliers
import io.github.srdjanv.endreforked.common.potions.EnderEyes
import io.github.srdjanv.endreforked.common.potions.TimedFlight
import io.github.srdjanv.endreforked.common.potions.base.BasePotion
import net.minecraft.potion.Potion
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.function.Supplier

object ModPotions {
    private val POTIONS = mutableListOf<Supplier<out BasePotion>>()

    @JvmField val ENDER_EYES: Supplier<BasePotion> = register(::EnderEyes)
    @JvmField val TIMED_FLIGHT: Supplier<BasePotion> = register(::TimedFlight)

    private fun <I : BasePotion> register(factory: () -> I): Supplier<I> =
        Suppliers.memoize { factory() }.also { POTIONS += it }

    @SubscribeEvent
    @JvmStatic
    fun onPotionRegister(event: RegistryEvent.Register<Potion>) {
        POTIONS.forEach { event.registry.register(it.get()) }
    }
}
