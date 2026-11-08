package io.github.srdjanv.endreforked.common.items.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.block.Block
import net.minecraft.item.ItemSeedFood

open class ItemSeedFoodBase(name: String, healAmount: Int, saturation: Float, crops: Block, soil: Block) :
    ItemSeedFood(healAmount, saturation, crops, soil), InventoryItemModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }
}
