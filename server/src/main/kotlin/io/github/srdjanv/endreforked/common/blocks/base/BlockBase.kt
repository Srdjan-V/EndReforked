package io.github.srdjanv.endreforked.common.blocks.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.Block
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material

open class BlockBase @JvmOverloads constructor(
    name: String,
    material: Material,
    mapColor: MapColor = material.materialMapColor,
) : Block(material, mapColor), InventoryBlockModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }
}
