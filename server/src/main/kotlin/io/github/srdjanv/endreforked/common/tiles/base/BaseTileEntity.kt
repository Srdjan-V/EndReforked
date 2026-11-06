package io.github.srdjanv.endreforked.common.tiles.base

import net.minecraft.inventory.InventoryHelper
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.items.ItemStackHandler

open class BaseTileEntity : TileEntity() {
    protected val inventoriesToDrop = mutableListOf<ItemStackHandler>()

    protected open inner class InternalItemStackHandler(size: Int) : ItemStackHandler(size) {
        override fun onContentsChanged(slot: Int) = markDirty()
    }

    fun dopItems() {
        for (handler in inventoriesToDrop) {
            for (i in 0 until handler.slots) {
                val stack = handler.getStackInSlot(i)
                if (!stack.isEmpty) InventoryHelper.spawnItemStack(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
            }
        }
    }
}
