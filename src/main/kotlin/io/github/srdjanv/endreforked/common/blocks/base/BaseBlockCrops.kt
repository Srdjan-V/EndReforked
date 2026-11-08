package io.github.srdjanv.endreforked.common.blocks.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.Block
import net.minecraft.block.BlockCrops
import net.minecraft.block.state.IBlockState

/** [sustainedBy] is resolved on the first [canSustainBush] call, so it may read other mod blocks. */
open class BaseBlockCrops(
    name: String, sustainedBy: () -> Set<Block> = ::emptySet,
) : BlockCrops(), InventoryBlockModel {
    private val sustainable by lazy(sustainedBy)

    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }

    override fun canSustainBush(state: IBlockState) = state.block in sustainable
}
