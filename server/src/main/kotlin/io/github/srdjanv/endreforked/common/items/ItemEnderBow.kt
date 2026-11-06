package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.items.tools.ToolBow
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.item.ItemStack

class ItemEnderBow(name: String) : ToolBow(name), InventoryItemModel {
    init {
        setMaxDamage(1024)
    }

    override fun getIsRepairable(toRepair: ItemStack, repair: ItemStack) =
        repair.item === ModItems.INGOT_ENDORIUM.get() || super.getIsRepairable(toRepair, repair)
}
