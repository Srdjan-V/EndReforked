package io.github.srdjanv.endreforked.common.blocks

import com.cleanroommc.modularui.factory.TileEntityGuiFactory
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.tiles.MaterializerTile
import io.github.srdjanv.endreforked.common.tiles.base.BaseTileEntity
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockMaterializer(name: String) : BlockBase(name, Material.IRON) {
    init {
        setDefaultState(blockState.baseState.withProperty(FACING, EnumFacing.NORTH).withProperty(WORKING, false))
    }

    override fun createBlockState() = BlockStateContainer(this, WORKING, FACING)

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ModItems.BLOCK_E_USER

    @SideOnly(Side.CLIENT)
    override fun getRenderLayer() = BlockRenderLayer.CUTOUT

    override fun isOpaqueCube(state: IBlockState) = false

    override fun isFullCube(state: IBlockState) = false

    override fun getItem(worldIn: World, pos: BlockPos, state: IBlockState) = ItemStack(ModBlocks.MATERIALIZER_BLOCK)

    override fun onBlockActivated(
        worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
        hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float,
    ): Boolean {
        if (!worldIn.isRemote) TileEntityGuiFactory.open(playerIn, pos)
        return true
    }

    override fun onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState) {
        if (worldIn.isRemote) return
        val north = worldIn.getBlockState(pos.north())
        val south = worldIn.getBlockState(pos.south())
        val west = worldIn.getBlockState(pos.west())
        val east = worldIn.getBlockState(pos.east())
        var face = state.getValue(FACING)
        if (face == EnumFacing.NORTH && north.isFullBlock && !south.isFullBlock) face = EnumFacing.SOUTH
        else if (face == EnumFacing.SOUTH && south.isFullBlock && !north.isFullBlock) face = EnumFacing.NORTH
        else if (face == EnumFacing.WEST && west.isFullBlock && !east.isFullBlock) face = EnumFacing.EAST
        else if (face == EnumFacing.EAST && east.isFullBlock && !west.isFullBlock) face = EnumFacing.WEST
        worldIn.setBlockState(pos, state.withProperty(FACING, face), 2)
    }

    override fun hasTileEntity(state: IBlockState) = true

    override fun createTileEntity(world: World, state: IBlockState): TileEntity? = MaterializerTile()

    override fun getStateForPlacement(
        world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float,
        meta: Int, placer: EntityLivingBase, hand: EnumHand,
    ): IBlockState = defaultState.withProperty(FACING, placer.horizontalFacing.opposite)

    override fun onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) {
        worldIn.setBlockState(pos, defaultState.withProperty(FACING, placer.horizontalFacing.opposite), 2)
    }

    override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
        (world.getTileEntity(pos) as? BaseTileEntity)?.dopItems()
        super.breakBlock(world, pos, state)
    }

    override fun getRenderType(state: IBlockState) = EnumBlockRenderType.MODEL

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

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        tooltip.add(I18n.format("tile.user.tooltip"))
    }

    companion object {
        @JvmField
        val FACING: PropertyDirection = BlockHorizontal.FACING

        @JvmField
        val WORKING: PropertyBool = PropertyBool.create("working")
    }
}
