package io.github.srdjanv.endreforked.utils

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

object PlayerUtils {
    /** Sources of player items. Compat mods add their own, for example baubles. */
    val inventories = mutableListOf<(EntityPlayer) -> List<ItemStack>>(
        { player -> List(player.inventory.sizeInventory) { player.inventory.getStackInSlot(it) } },
    )

    @JvmStatic
    fun getAllPlayerItems(player: EntityPlayer): List<ItemStack> = inventories.flatMap { it(player) }
}
