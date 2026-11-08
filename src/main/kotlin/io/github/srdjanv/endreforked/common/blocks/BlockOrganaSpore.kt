package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockBush
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.IBlockAccess

/** Small mushroom-like spore growth found on Organa surfaces and floating islands. */
class BlockOrganaSpore : BaseBlockBush("organa_spore", Material.PLANTS, { setOf(ModBlocks.ORGANA_BLOCK, ModBlocks.ORGANA_END_STONE_BLOCK) }) {
    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: net.minecraft.util.math.BlockPos) = AABB

    companion object {
        private val AABB = AxisAlignedBB(0.2, 0.0, 0.2, 0.8, 0.45, 0.8)
    }
}
