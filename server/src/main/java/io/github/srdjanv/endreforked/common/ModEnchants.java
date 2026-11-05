package io.github.srdjanv.endreforked.common;

import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import io.github.srdjanv.endreforked.common.enchants.EnchantEnderCore;
import io.github.srdjanv.endreforked.common.enchants.EnchantEnderKiller;
import io.github.srdjanv.endreforked.common.enchants.EnchantShulkerCore;

@SuppressWarnings("unused")
public final class ModEnchants {

    public static final Enchantment ender_core = new EnchantEnderCore();
    public static final Enchantment ender_killer = new EnchantEnderKiller();
    public static final Enchantment shulker_core = new EnchantShulkerCore();

    @SubscribeEvent
    public static void onEvent(final RegistryEvent.Register<Enchantment> event) {
        final IForgeRegistry<Enchantment> registry = event.getRegistry();

        registry.register(ender_core);
        registry.register(ender_killer);
        registry.register(shulker_core);
    }
}
