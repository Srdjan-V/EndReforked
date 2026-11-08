package io.github.srdjanv.endreforked.common.blocks.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.Block
import net.minecraft.block.BlockFenceGate
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

open class BlockWallBase(name: String, main: Block) : Block(main.getMaterial(main.defaultState)), InventoryBlockModel {
    init {
        setDefaultState(
            blockState.baseState.withProperty(UP, true).withProperty(NORTH, false)
                .withProperty(EAST, false).withProperty(SOUTH, false).withProperty(WEST, false)
        )
        setHardness(main.getBlockHardness(main.defaultState, null, null))
        setSoundType(main.getSoundType())
        setRegistryName(name)
        setTranslationKey(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) =
        AABB_BY_INDEX[aabbIndex(getActualState(state, source, pos))]

    override fun addCollisionBoxToList(
        state: IBlockState, worldIn: World, pos: BlockPos, entityBox: AxisAlignedBB,
        collidingBoxes: MutableList<AxisAlignedBB>, entityIn: Entity?, isActualState: Boolean,
    ) {
        val actual = if (isActualState) state else getActualState(state, worldIn, pos)
        addCollisionBoxToList(pos, entityBox, collidingBoxes, CLIP_AABB_BY_INDEX[aabbIndex(actual)])
    }

    override fun getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos) =
        CLIP_AABB_BY_INDEX[aabbIndex(getActualState(blockState, worldIn, pos))]

    @SideOnly(Side.CLIENT)
    override fun getSubBlocks(itemIn: CreativeTabs, list: NonNullList<ItemStack>) {
        list.add(ItemStack(this))
    }

    override fun getMetaFromState(state: IBlockState) = 0

    override fun damageDropped(state: IBlockState) = 0

    override fun isFullCube(state: IBlockState) = false

    override fun isPassable(worldIn: IBlockAccess, pos: BlockPos) = false

    override fun isOpaqueCube(state: IBlockState) = false

    override fun getActualState(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): IBlockState {
        val north = canWallConnectTo(worldIn, pos, EnumFacing.NORTH)
        val east = canWallConnectTo(worldIn, pos, EnumFacing.EAST)
        val south = canWallConnectTo(worldIn, pos, EnumFacing.SOUTH)
        val west = canWallConnectTo(worldIn, pos, EnumFacing.WEST)
        val straight = north && !east && south && !west || !north && east && !south && west
        return state.withProperty(UP, !straight || !worldIn.isAirBlock(pos.up()))
            .withProperty(NORTH, north).withProperty(EAST, east)
            .withProperty(SOUTH, south).withProperty(WEST, west)
    }

    override fun canBeConnectedTo(world: IBlockAccess, pos: BlockPos, facing: EnumFacing) =
        canConnectTo(world, pos.offset(facing), facing.opposite)

    private fun canWallConnectTo(world: IBlockAccess, pos: BlockPos, facing: EnumFacing): Boolean {
        val other = pos.offset(facing)
        return world.getBlockState(other).block.canBeConnectedTo(world, other, facing.opposite) ||
            canConnectTo(world, other, facing.opposite)
    }

    override fun createBlockState() = BlockStateContainer(this, UP, NORTH, EAST, WEST, SOUTH)

    private fun canConnectTo(world: IBlockAccess, pos: BlockPos, facing: EnumFacing): Boolean {
        val state = world.getBlockState(pos)
        val block = state.block
        val shape = state.getBlockFaceShape(world, pos, facing)
        val pole = shape == BlockFaceShape.MIDDLE_POLE_THICK ||
            shape == BlockFaceShape.MIDDLE_POLE && block is BlockFenceGate
        return !isExcepBlockForAttachWithPiston(block) && shape == BlockFaceShape.SOLID || pole
    }

    @SideOnly(Side.CLIENT)
    override fun shouldSideBeRendered(
        blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing,
    ) = side != EnumFacing.DOWN || super.shouldSideBeRendered(blockState, blockAccess, pos, side)

    override fun getStateFromMeta(meta: Int): IBlockState = defaultState

    private companion object {
        val UP: PropertyBool = PropertyBool.create("up")
        val NORTH: PropertyBool = PropertyBool.create("north")
        val EAST: PropertyBool = PropertyBool.create("east")
        val SOUTH: PropertyBool = PropertyBool.create("south")
        val WEST: PropertyBool = PropertyBool.create("west")

        val AABB_BY_INDEX = arrayOf(
            AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 1.0, 0.75),
            AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 1.0, 1.0),
            AxisAlignedBB(0.0, 0.0, 0.25, 0.75, 1.0, 0.75),
            AxisAlignedBB(0.0, 0.0, 0.25, 0.75, 1.0, 1.0),
            AxisAlignedBB(0.25, 0.0, 0.0, 0.75, 1.0, 0.75),
            AxisAlignedBB(0.3125, 0.0, 0.0, 0.6875, 0.875, 1.0),
            AxisAlignedBB(0.0, 0.0, 0.0, 0.75, 1.0, 0.75),
            AxisAlignedBB(0.0, 0.0, 0.0, 0.75, 1.0, 1.0),
            AxisAlignedBB(0.25, 0.0, 0.25, 1.0, 1.0, 0.75),
            AxisAlignedBB(0.25, 0.0, 0.25, 1.0, 1.0, 1.0),
            AxisAlignedBB(0.0, 0.0, 0.3125, 1.0, 0.875, 0.6875),
            AxisAlignedBB(0.0, 0.0, 0.25, 1.0, 1.0, 1.0),
            AxisAlignedBB(0.25, 0.0, 0.0, 1.0, 1.0, 0.75),
            AxisAlignedBB(0.25, 0.0, 0.0, 1.0, 1.0, 1.0),
            AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 0.75),
            AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0),
        )
        val CLIP_AABB_BY_INDEX = Array(AABB_BY_INDEX.size) { AABB_BY_INDEX[it].setMaxY(1.5) }

        fun aabbIndex(state: IBlockState): Int {
            var i = 0
            if (state.getValue(NORTH)) i = i or (1 shl EnumFacing.NORTH.horizontalIndex)
            if (state.getValue(EAST)) i = i or (1 shl EnumFacing.EAST.horizontalIndex)
            if (state.getValue(SOUTH)) i = i or (1 shl EnumFacing.SOUTH.horizontalIndex)
            if (state.getValue(WEST)) i = i or (1 shl EnumFacing.WEST.horizontalIndex)
            return i
        }

        fun isExcepBlockForAttachWithPiston(block: Block) =
            Block.isExceptBlockForAttachWithPiston(block) || block == Blocks.BARRIER || block == Blocks.MELON_BLOCK ||
                block == Blocks.PUMPKIN || block == Blocks.LIT_PUMPKIN
    }
}
