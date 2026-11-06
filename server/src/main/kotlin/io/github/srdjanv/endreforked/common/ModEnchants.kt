package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.common.enchants.EnchantEnderCore
import io.github.srdjanv.endreforked.common.enchants.EnchantEnderKiller
import io.github.srdjanv.endreforked.common.enchants.EnchantShulkerCore
import net.minecraft.enchantment.Enchantment
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ModEnchants {
    @JvmField val ender_core: Enchantment = EnchantEnderCore()
    @JvmField val ender_killer: Enchantment = EnchantEnderKiller()
    @JvmField val shulker_core: Enchantment = EnchantShulkerCore()

    @SubscribeEvent
    @JvmStatic
    fun onEvent(event: RegistryEvent.Register<Enchantment>) {
        event.registry.registerAll(ender_core, ender_killer, shulker_core)
    }
}
