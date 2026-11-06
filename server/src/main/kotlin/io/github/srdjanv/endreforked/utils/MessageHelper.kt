package io.github.srdjanv.endreforked.utils

import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World

object MessageHelper {
    @JvmStatic
    fun customGroup(world: World, text: String) {
        for (player in world.playerEntities) player.sendMessage(TextComponentString(text))
    }
}
