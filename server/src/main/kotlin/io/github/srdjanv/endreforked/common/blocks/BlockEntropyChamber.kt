package io.github.srdjanv.endreforked.common.blocks

import com.cleanroommc.modularui.factory.TileEntityGuiFactory
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.tiles.EntropyChamberTile
import io.github.srdjanv.endreforked.common.tiles.base.BaseTileEntity
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class BlockEntropyChamber : BlockBase("entropy_chamber", Material.IRON) {
    init {
        setDefaultState(
            blockState.baseState
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(WORKING_ITEM, false)
                .withProperty(WORKING_FLUID, false)
        )
    }

    override fun createBlockState() = BlockStateContainer(this, FACING, WORKING_ITEM, WORKING_FLUID)

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ModItems.ENTROPY_CHAMBER.get()

    override fun hasTileEntity(state: IBlockState) = true

    override fun createTileEntity(world: World, state: IBlockState): TileEntity? = EntropyChamberTile()

    override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
        (world.getTileEntity(pos) as? BaseTileEntity)?.dopItems()
        super.breakBlock(world, pos, state)
    }

    override fun getItem(worldIn: World, pos: BlockPos, state: IBlockState) = ItemStack(ModBlocks.ENTROPY_CHAMBER.get())

    override fun onBlockActivated(
        worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
        hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float,
    ): Boolean {
        if (!worldIn.isRemote) TileEntityGuiFactory.open(playerIn, pos)
        return true
    }

    override fun getRenderType(state: IBlockState) = EnumBlockRenderType.MODEL

    override fun getStateForPlacement(
        world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float,
        meta: Int, placer: EntityLivingBase, hand: EnumHand,
    ): IBlockState = defaultState.withProperty(FACING, placer.horizontalFacing.opposite)

    override fun onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) {
        worldIn.setBlockState(pos, defaultState.withProperty(FACING, placer.horizontalFacing.opposite), 2)
    }

    override fun withRotation(state: IBlockState, rot: Rotation): IBlockState =
        state.withProperty(FACING, rot.rotate(state.getValue(FACING)))

    override fun withMirror(state: IBlockState, mirrorIn: Mirror): IBlockState =
        state.withRotation(mirrorIn.toRotation(state.getValue(FACING)))

    override fun getStateFromMeta(meta: Int): IBlockState {
        var facing = EnumFacing.byHorizontalIndex(meta)
        if (facing.axis == EnumFacing.Axis.Y) facing = EnumFacing.NORTH
        return defaultState.withProperty(FACING, facing)
    }

    override fun getMetaFromState(state: IBlockState) = state.getValue(FACING).index

    companion object {
        @JvmField
        val FACING: PropertyDirection = BlockHorizontal.FACING

        @JvmField
        val WORKING_ITEM: PropertyBool = PropertyBool.create("working_item")

        @JvmField
        val WORKING_FLUID: PropertyBool = PropertyBool.create("working_fluid")
    }
}
