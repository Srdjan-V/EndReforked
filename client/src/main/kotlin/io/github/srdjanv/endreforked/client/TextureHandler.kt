package io.github.srdjanv.endreforked.client

import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.TextureStitchEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TextureHandler {
    private val textures = LinkedHashSet<ResourceLocation>()

    fun registerTexture(location: ResourceLocation) {
        textures += location
    }

    @JvmStatic
    @SubscribeEvent
    fun onRegisterTextures(event: TextureStitchEvent.Pre) {
        for (texture in textures) event.map.registerSprite(texture)
    }
}
