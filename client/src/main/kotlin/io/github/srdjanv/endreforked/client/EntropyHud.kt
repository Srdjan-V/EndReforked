package io.github.srdjanv.endreforked.client

import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.utils.PlayerUtils
import io.github.srdjanv.endreforked.wire.s2c.ChunkEntropy
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.resources.I18n
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/** Top left overlay with the chunk entropy level. Only while the player carries the Lormyte charm (inventory, offhand or Baubles). */
class EntropyHud(private val entropy: () -> ChunkEntropy?) {
    @SubscribeEvent
    fun onOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return
        val data = entropy() ?: return
        val mc = Minecraft.getMinecraft()
        val player = mc.player ?: return
        val charm = ModItems.LORMYTE_CHARM.get()
        // PlayerUtils covers the main inventory, offhand, armor and Baubles slots
        if (PlayerUtils.getAllPlayerItems(player).none { it.item === charm }) return

        val level = EntropyLevel.values().getOrElse(data.level) { EntropyLevel.STABLE }
        val color = when (level) {
            EntropyLevel.STABLE -> 0xFF55FF55.toInt()
            EntropyLevel.CHARGED -> 0xFFFFAA00.toInt()
            EntropyLevel.CRITICAL -> 0xFFFF5555.toInt()
        }
        val font = mc.fontRenderer
        val x = 4
        val y = 4
        val name = I18n.format("hud.entropy.level.${level.name.lowercase()}")
        font.drawStringWithShadow(name, x.toFloat(), y.toFloat(), color)
        font.drawStringWithShadow(I18n.format("hud.entropy.chunk", "${data.entropy} / ${data.capacity}"), x.toFloat(), (y + 10).toFloat(), 0xFFFFFFFF.toInt())
        val width = 60
        val filled = if (data.capacity > 0) (width * data.entropy.toLong() / data.capacity).toInt().coerceIn(0, width) else 0
        Gui.drawRect(x, y + 21, x + width, y + 25, 0xFF000000.toInt())
        Gui.drawRect(x, y + 21, x + filled, y + 25, color)
    }
}
