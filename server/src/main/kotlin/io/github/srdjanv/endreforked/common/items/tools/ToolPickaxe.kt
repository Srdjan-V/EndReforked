package io.github.srdjanv.endreforked.common.items.tools

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.item.ItemPickaxe
import net.minecraft.item.Item.ToolMaterial

class ToolPickaxe(name: String, material: ToolMaterial) : ItemPickaxe(material), InventoryItemModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }
}
