package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import net.minecraft.block.state.IBlockState
import net.minecraft.world.IBlockAccess
import net.minecraft.util.math.BlockPos
import net.minecraft.block.material.Material
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumFacing
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.world.World

class EntropyLantern(name: String) : BlockBase(name, Material.ROCK) {

    init {
        setLightLevel(0.0f)
        setLightOpacity(0)
        setSoundType(SoundType.WOOD)
        setHardness(3.5f)
        setHarvestLevel("pickaxe", 1)
        setTickRandomly(true)
        defaultState = blockState.baseState.withProperty(HANGING, false).withProperty(LIGHT_LEVEL, 0)
    }

    override fun isOpaqueCube(state: IBlockState) = false
    override fun isFullCube(state: IBlockState) = false
    override fun getRenderLayer() = BlockRenderLayer.CUTOUT

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
        if (state.getValue(HANGING)) HANGING_AABB else STANDING_AABB

    override fun getStateForPlacement(
        world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float,
        meta: Int, placer: EntityLivingBase,
    ): IBlockState = defaultState.withProperty(HANGING, facing == EnumFacing.DOWN)

    override fun randomTick(world: World, pos: BlockPos, state: IBlockState, random: java.util.Random) {
        if (world.isRemote) return
        val next = lightForChunk(world, pos)
        if (state.getValue(LIGHT_LEVEL) != next) {
            world.setBlockState(pos, state.withProperty(LIGHT_LEVEL, next), 3)
        }
    }

    override fun canPlaceBlockAt(worldIn: World, pos: BlockPos): Boolean =
        worldIn.isSideSolid(pos.down(), EnumFacing.UP) || worldIn.isSideSolid(pos.up(), EnumFacing.DOWN)

    override fun neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos) {
        if (!canPlaceBlockAt(worldIn, pos)) worldIn.destroyBlock(pos, true)
    }

    override fun createBlockState() = BlockStateContainer(this, HANGING, LIGHT_LEVEL)

    override fun getMetaFromState(state: IBlockState) =
        (if (state.getValue(HANGING)) 4 else 0) + state.getValue(LIGHT_LEVEL)

    override fun getStateFromMeta(meta: Int) = defaultState
        .withProperty(HANGING, meta and 4 != 0)
        .withProperty(LIGHT_LEVEL, (meta and 3).coerceIn(0, 2))

    override fun getLightValue(state: IBlockState, world: IBlockAccess, pos: BlockPos): Int {
        return state.getValue(LIGHT_LEVEL)
    }

    private fun lightForChunk(world: World, pos: BlockPos): Int = when (
        EntropyWorldHandler.getEntropyChunkWorld(world.provider.dimension, pos)
            .map { EntropyTuning.level(it.currentEntropy, it.maxEntropy) }
            .orElse(io.github.srdjanv.endreforked.api.entropy.EntropyLevel.STABLE)
    ) {
        io.github.srdjanv.endreforked.api.entropy.EntropyLevel.STABLE -> 0
        io.github.srdjanv.endreforked.api.entropy.EntropyLevel.CHARGED -> 1
        io.github.srdjanv.endreforked.api.entropy.EntropyLevel.CRITICAL -> 2
    }

    companion object {
        val HANGING: PropertyBool = PropertyBool.create("hanging")
        val LIGHT_LEVEL: PropertyInteger = PropertyInteger.create("light_level", 0, 2)
        private val STANDING_AABB = AxisAlignedBB(5.0 / 16, 0.0, 5.0 / 16, 11.0 / 16, 13.0 / 16, 11.0 / 16)
        private val HANGING_AABB = AxisAlignedBB(5.0 / 16, 1.0 / 16, 5.0 / 16, 11.0 / 16, 1.0, 11.0 / 16)
    }
}
