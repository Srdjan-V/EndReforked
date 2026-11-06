package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockEnderCropDead : BlockBase("ender_flower_crop_dead", Material.PLANTS) {
    init {
        setSoundType(SoundType.PLANT)
        setHardness(0.0F)
        disableStats()
    }

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = Items.AIR

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) = CROPS_AABB

    override fun getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB? =
        NULL_AABB

    override fun isOpaqueCube(state: IBlockState) = false

    override fun isFullCube(state: IBlockState) = false

    @SideOnly(Side.CLIENT)
    override fun getRenderLayer() = BlockRenderLayer.CUTOUT

    override fun getBlockFaceShape(worldIn: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) =
        BlockFaceShape.UNDEFINED

    private companion object {
        val CROPS_AABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
    }
}
