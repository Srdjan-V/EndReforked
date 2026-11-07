package io.github.srdjanv.endreforked.client

import io.github.srdjanv.endreforked.EndReforked
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.ReflectionHelper

object GuiMainMenuEnd {
    fun endMainMenu() {
        val field = ReflectionHelper.findField(GuiMainMenu::class.java, "TITLE_PANORAMA_PATHS", "field_73978_o", "o")
        try {
            // The field is static final, but the array is mutable: write its elements.
            @Suppress("UNCHECKED_CAST")
            val paths = field.get(null) as Array<ResourceLocation>
            for (i in paths.indices) paths[i] = ResourceLocation("textures/gui/title/background/end_panorama_$i.png")
        } catch (e: Exception) {
            EndReforked.LOGGER.error(e)
        }
    }
}
