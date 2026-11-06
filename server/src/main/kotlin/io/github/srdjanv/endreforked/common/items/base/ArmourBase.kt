package io.github.srdjanv.endreforked.common.items.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemArmor.ArmorMaterial

open class ArmourBase(name: String, material: ArmorMaterial, renderIndex: Int, slot: EntityEquipmentSlot) :
    ItemArmor(material, renderIndex, slot), InventoryItemModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }
}
