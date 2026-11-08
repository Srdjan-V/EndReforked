package io.github.srdjanv.endreforked.common.blocks.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.Block
import net.minecraft.block.BlockStairs

open class BlockStairsBase(name: String, block: Block) : BlockStairs(block.defaultState), InventoryBlockModel {
    init {
        setTranslationKey(name)
        setHardness(block.getBlockHardness(block.defaultState, null, null))
        setSoundType(block.getSoundType())
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
        useNeighborBrightness = true
    }
}
