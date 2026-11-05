package io.github.srdjanv.endreforked.common;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import io.github.srdjanv.endreforked.common.sounds.EndSound;

public final class ModSounds {

    public static final EndSound THE_VOID = new EndSound("the_void");

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> register) {
        register.getRegistry().register(THE_VOID);
    }
}
