package io.github.srdjanv.endreforked.common.blocks.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.Block
import net.minecraft.block.BlockBush
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState

open class BaseBlockBush(name: String, material: Material) : BlockBush(material), InventoryBlockModel {
    @JvmField
    protected val sustainableBlocks: MutableList<Block> = ArrayList()

    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }

    override fun canSustainBush(state: IBlockState) = state.block in sustainableBlocks
}
