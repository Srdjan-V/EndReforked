package io.github.srdjanv.endreforked.common.items.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.block.Block
import net.minecraft.item.ItemSeeds

open class BaseSeeds(name: String, crops: Block, soil: Block) : ItemSeeds(crops, soil), InventoryItemModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }
}
