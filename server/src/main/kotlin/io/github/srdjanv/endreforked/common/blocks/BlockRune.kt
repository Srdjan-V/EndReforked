package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.Block
import net.minecraft.block.material.Material

class BlockRune(name: String, material: Material) : Block(material), InventoryBlockModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setHardness(5.0f)
        setCreativeTab(EndReforked.ENDERTAB)
    }
}
