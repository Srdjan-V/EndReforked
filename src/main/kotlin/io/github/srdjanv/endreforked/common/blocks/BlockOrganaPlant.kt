package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import net.minecraft.block.Block
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockOrganaPlant : BlockBase("organa_plant", Material.PLANTS, MapColor.PURPLE) {

    init {
        setDefaultState(
            blockState.baseState
                .withProperty(NORTH, false).withProperty(EAST, false).withProperty(SOUTH, false)
                .withProperty(WEST, false).withProperty(UP, false).withProperty(DOWN, false),
        )
        setHarvestLevel("pickaxe", 1)
        setHardness(1.25f)
    }

    override fun getActualState(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): IBlockState {
        fun at(p: BlockPos) = worldIn.getBlockState(p).block
        val flower = ModBlocks.ORGANA_FLOWER_BLOCK
        fun connects(p: BlockPos) = at(p).let { it === this || it === flower }
        return state
            .withProperty(DOWN, at(pos.down()).let { it === this || it === flower || it === ModBlocks.END_MOSS_GRASS_BLOCK })
            .withProperty(UP, connects(pos.up()))
            .withProperty(NORTH, connects(pos.north()))
            .withProperty(EAST, connects(pos.east()))
            .withProperty(SOUTH, connects(pos.south()))
            .withProperty(WEST, connects(pos.west()))
    }

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB {
        val s = state.getActualState(source, pos)
        return AxisAlignedBB(
            if (s.getValue(WEST)) 0.0 else 0.1875,
            if (s.getValue(DOWN)) 0.0 else 0.1875,
            if (s.getValue(NORTH)) 0.0 else 0.1875,
            if (s.getValue(EAST)) 1.0 else 0.8125,
            if (s.getValue(UP)) 1.0 else 0.8125,
            if (s.getValue(SOUTH)) 1.0 else 0.8125,
        )
    }

    override fun addCollisionBoxToList(
        state: IBlockState, worldIn: World, pos: BlockPos, entityBox: AxisAlignedBB,
        collidingBoxes: MutableList<AxisAlignedBB>, entityIn: Entity?, isActualState: Boolean,
    ) {
        val s = if (isActualState) state else state.getActualState(worldIn, pos)

        fun box(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double) =
            addCollisionBoxToList(pos, entityBox, collidingBoxes, AxisAlignedBB(x1, y1, z1, x2, y2, z2))

        box(0.1875, 0.1875, 0.1875, 0.8125, 0.8125, 0.8125)
        if (s.getValue(WEST)) box(0.0, 0.1875, 0.1875, 0.1875, 0.8125, 0.8125)
        if (s.getValue(EAST)) box(0.8125, 0.1875, 0.1875, 1.0, 0.8125, 0.8125)
        if (s.getValue(UP)) box(0.1875, 0.8125, 0.1875, 0.8125, 1.0, 0.8125)
        if (s.getValue(DOWN)) box(0.1875, 0.0, 0.1875, 0.8125, 0.1875, 0.8125)
        if (s.getValue(NORTH)) box(0.1875, 0.1875, 0.0, 0.8125, 0.8125, 0.1875)
        if (s.getValue(SOUTH)) box(0.1875, 0.1875, 0.8125, 0.8125, 0.8125, 1.0)
    }

    override fun getMetaFromState(state: IBlockState) = 0

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        if (!canSurviveAt(worldIn, pos)) worldIn.destroyBlock(pos, true)
    }

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ModItems.ORGANA_FRUIT

    override fun quantityDropped(random: Random) = random.nextInt(2)

    override fun isFullCube(state: IBlockState) = false

    override fun isOpaqueCube(state: IBlockState) = false

    override fun canPlaceBlockAt(worldIn: World, pos: BlockPos) =
        super.canPlaceBlockAt(worldIn, pos) && canSurviveAt(worldIn, pos)

    override fun neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos) {
        if (worldIn.isRemote) return
        if (!canSurviveAt(worldIn, pos)) worldIn.scheduleUpdate(pos, this, 1)
    }

    fun canSurviveAt(wordIn: World, pos: BlockPos): Boolean {
        val upAir = wordIn.isAirBlock(pos.up())
        val downAir = wordIn.isAirBlock(pos.down())

        for (enumfacing in EnumFacing.Plane.HORIZONTAL) {
            val blockPosFacing = pos.offset(enumfacing)
            if (wordIn.getBlockState(blockPosFacing).block === this) {
                if (!upAir && !downAir) return false
                val blockUnder = wordIn.getBlockState(blockPosFacing.down()).block
                if (blockUnder === this || blockUnder === ModBlocks.END_MOSS_GRASS_BLOCK) return true
            }
        }

        val blockDown = wordIn.getBlockState(pos.down()).block
        return blockDown === this || blockDown === ModBlocks.END_MOSS_GRASS_BLOCK
    }

    override fun createBlockState() = BlockStateContainer(this, NORTH, EAST, SOUTH, WEST, UP, DOWN)

    override fun isPassable(worldIn: IBlockAccess, pos: BlockPos) = false

    @SideOnly(Side.CLIENT)
    override fun getRenderLayer() = BlockRenderLayer.CUTOUT

    @SideOnly(Side.CLIENT)
    override fun shouldSideBeRendered(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Boolean {
        val block = blockAccess.getBlockState(pos.offset(side)).block
        return block !== this && block !== Blocks.CHORUS_FLOWER && (side != EnumFacing.DOWN || block !== Blocks.END_STONE)
    }

    override fun getBlockFaceShape(worldIn: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) =
        BlockFaceShape.UNDEFINED

    companion object {
        @JvmField val NORTH: PropertyBool = PropertyBool.create("north")
        @JvmField val EAST: PropertyBool = PropertyBool.create("east")
        @JvmField val SOUTH: PropertyBool = PropertyBool.create("south")
        @JvmField val WEST: PropertyBool = PropertyBool.create("west")
        @JvmField val UP: PropertyBool = PropertyBool.create("up")
        @JvmField val DOWN: PropertyBool = PropertyBool.create("down")
    }
}
