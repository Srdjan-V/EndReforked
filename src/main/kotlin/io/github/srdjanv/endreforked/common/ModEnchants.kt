package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.common.enchants.EnchantEnderCore
import io.github.srdjanv.endreforked.common.enchants.EnchantEnderKiller
import io.github.srdjanv.endreforked.common.enchants.EnchantShulkerCore
import net.minecraft.enchantment.Enchantment
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ModEnchants {
    private val ENCHANTS = DeferredRegistry(this, Enchantment::class.java)

    @get:JvmStatic val ender_core by ENCHANTS.register(::EnchantEnderCore)
    @get:JvmStatic val ender_killer by ENCHANTS.register(::EnchantEnderKiller)
    @get:JvmStatic val shulker_core by ENCHANTS.register(::EnchantShulkerCore)

    @SubscribeEvent
    @JvmStatic
    fun onEvent(event: RegistryEvent.Register<Enchantment>) = ENCHANTS.registerAll(event.registry)
}
