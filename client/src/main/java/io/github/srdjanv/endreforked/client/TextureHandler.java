package io.github.srdjanv.endreforked.client;

import java.util.Set;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

public final class TextureHandler {

    private static final Set<ResourceLocation> TEXTURES = new ObjectArraySet<>(10);

    public static void registerTexture(ResourceLocation location) {
        TEXTURES.add(location);
    }

    @SubscribeEvent
    static void onRegisterTextures(TextureStitchEvent.Pre event) {
        for (ResourceLocation fluidTexture : TEXTURES) {
            event.getMap().registerSprite(fluidTexture);
        }
    }
}
