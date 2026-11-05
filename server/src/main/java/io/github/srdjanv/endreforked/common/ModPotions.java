package io.github.srdjanv.endreforked.common;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.base.Suppliers;

import io.github.srdjanv.endreforked.common.potions.EnderEyes;
import io.github.srdjanv.endreforked.common.potions.TimedFlight;
import io.github.srdjanv.endreforked.common.potions.base.BasePotion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class ModPotions {

    private static final List<Supplier<? extends BasePotion>> POTIONS = new ObjectArrayList<>();

    public static final Supplier<BasePotion> ENDER_EYES = register(EnderEyes::new);
    public static final Supplier<BasePotion> TIMED_FLIGHT = register(TimedFlight::new);

    public static <I extends BasePotion> Supplier<I> register(final com.google.common.base.Supplier<I> supplier) {
        var memorized = Suppliers.memoize(supplier);
        POTIONS.add(memorized);
        return memorized;
    }

    @SubscribeEvent
    static void onPotionRegister(RegistryEvent.Register<Potion> event) {
        var registry = event.getRegistry();
        POTIONS.stream().map(Supplier::get)
                .filter(Objects::nonNull)
                .forEach(registry::register);
    }
}
