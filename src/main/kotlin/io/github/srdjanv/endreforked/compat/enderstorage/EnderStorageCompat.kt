package io.github.srdjanv.endreforked.compat.enderstorage

import codechicken.enderstorage.api.Frequency
import codechicken.enderstorage.item.ItemEnderPouch
import codechicken.enderstorage.manager.EnderStorageManager
import codechicken.enderstorage.storage.EnderItemStorage
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.InvWrapper

/**
 * Ender Storage (CodeChicken), optional. This is the only file that touches its classes.
 * Call it only when `Loader.isModLoaded(MOD_ID)`, else the classes are missing.
 */
object EnderStorageCompat {
    const val MOD_ID = "enderstorage"

    /** The ender chest of the first Ender Pouch that the player carries (main inventory or offhand), as an item handler. Null without a pouch. */
    fun pouchChest(player: EntityPlayer): IItemHandler? {
        val pouch = (player.inventory.mainInventory + player.inventory.offHandInventory).firstOrNull { it.item is ItemEnderPouch } ?: return null
        val storage = EnderStorageManager.instance(player.world.isRemote).getStorage(Frequency.readFromStack(pouch), "item") as? EnderItemStorage
        return storage?.let(::InvWrapper)
    }
}
