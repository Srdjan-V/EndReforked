package io.github.srdjanv.endreforked.common.items.tools

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.item.Item.ToolMaterial
import net.minecraft.item.ItemAxe

class ToolAxe(name: String, material: ToolMaterial) : ItemAxe(material, 0f, 0f), InventoryItemModel {
    init {
        // The old Java code picked these values by "material is instance of the endorium material's class".
        // Both materials share the ToolMaterial class, so that branch always matched. Kept as is.
        attackDamage = 9f
        attackSpeed = -3.1f
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }
}
