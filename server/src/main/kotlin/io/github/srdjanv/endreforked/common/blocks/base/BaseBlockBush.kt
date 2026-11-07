package io.github.srdjanv.endreforked.common.blocks.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.Block
import net.minecraft.block.BlockBush
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks

/** The soils the end plants grow on. Read lazily: the blocks do not exist while a plant is constructed. */
internal fun endSoils(): Set<Block> =
    setOf(Blocks.END_BRICKS, Blocks.END_STONE, ModBlocks.ENTROPY_END_STONE, ModBlocks.ENTROPY_CROP_BLOCK)

/** [sustainedBy] is resolved on the first [canSustainBush] call, so it may read other mod blocks. */
open class BaseBlockBush(
    name: String, material: Material, sustainedBy: () -> Set<Block> = ::emptySet,
) : BlockBush(material), InventoryBlockModel {
    private val sustainable by lazy(sustainedBy)

    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }

    override fun canSustainBush(state: IBlockState) = state.block in sustainable
}
