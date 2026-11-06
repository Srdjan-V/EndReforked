package io.github.srdjanv.endreforked.common.blocks.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.Block
import net.minecraft.block.BlockCrops
import net.minecraft.block.state.IBlockState

open class BaseBlockCrops(name: String) : BlockCrops(), InventoryBlockModel {
    @JvmField
    protected val sustainableBlocks: MutableList<Block> = ArrayList()

    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }

    override fun canSustainBush(state: IBlockState) = state.block in sustainableBlocks
}
