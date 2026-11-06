package io.github.srdjanv.endreforked.common.items.tools

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.item.ItemHoe
import net.minecraft.item.Item.ToolMaterial

class ToolHoe(name: String, material: ToolMaterial) : ItemHoe(material), InventoryItemModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }
}
