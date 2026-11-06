package io.github.srdjanv.endreforked.common.items.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.item.ItemFood

open class ItemFoodBase(name: String, amount: Int, saturation: Float, isWolfFood: Boolean) :
    ItemFood(amount, saturation, isWolfFood), InventoryItemModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }
}
