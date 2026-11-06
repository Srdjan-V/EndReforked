package io.github.srdjanv.endreforked.client

import io.github.srdjanv.endreforked.EndReforked
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.ReflectionHelper
import java.lang.reflect.Field
import java.lang.reflect.Modifier

object GuiMainMenuEnd {
    fun endMainMenu() {
        val paths = Array(6) { ResourceLocation("textures/gui/title/background/end_panorama_$it.png") }
        val field = ReflectionHelper.findField(GuiMainMenu::class.java, "TITLE_PANORAMA_PATHS", "field_73978_o", "o")
        try {
            // Removes the final modifier. This only works on Java 11 and older, newer versions throw and the error is logged.
            Field::class.java.getDeclaredField("modifiers").apply { isAccessible = true }
                .setInt(field, field.modifiers and Modifier.FINAL.inv())
            field.isAccessible = true
            field.set(null, paths)
        } catch (e: Exception) {
            EndReforked.LOGGER.error(e)
        }
    }
}
