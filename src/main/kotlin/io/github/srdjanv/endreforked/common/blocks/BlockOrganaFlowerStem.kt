package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockCrops
import net.minecraft.block.BlockCrops
import net.minecraft.block.BlockTorch
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.fluids.BlockFluidBase
import java.util.Random

class BlockOrganaFlowerStem : BaseBlockCrops("organa_flower_stem", { setOf(ModBlocks.ORGANA_BLOCK, ModBlocks.ORGANA_SENSOR_SOIL) }) {

    init {
        setTickRandomly(true)
        defaultState = blockState.baseState.withProperty(AGE, 0).withProperty(FACING, EnumFacing.UP)
    }

    override fun createBlockState() = BlockStateContainer(this, AGE, FACING)

    override fun getSeed(): Item = ModItems.ORGANA_FLOWER_SEED

    override fun getCrop(): Item = ModItems.ORGANA_FLOWER

    override fun isMaxAge(state: IBlockState) = state.getValue(AGE) >= 7

    override fun getMaxAge() = 7

    override fun getAgeProperty(): PropertyInteger = AGE

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
        if (getActualState(state, source, pos).getValue(FACING) == EnumFacing.UP) {
            STEM_AABB[minOf(state.getValue(AGE), maxAge)]
        } else STEM_AABB[4]

    override fun getActualState(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): IBlockState {
        var result = state.withProperty(FACING, EnumFacing.UP)

        for (facing in EnumFacing.Plane.HORIZONTAL) {
            if (worldIn.getBlockState(pos.offset(facing)).block === FLOWER && isMaxAge(result)) {
                result = result.withProperty(FACING, facing)
                break
            }
        }

        return result
    }

    override fun harvestBlock(
        worldIn: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, te: TileEntity?, stack: ItemStack,
    ) {}

    override fun getDrops(
        drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int,
    ) {}

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = Items.AIR

    override fun quantityDropped(random: Random) = 0

    override fun canBlockStay(world: World, pos: BlockPos, state: IBlockState): Boolean {
        // Forge: This function is called during world gen and placement, before this block is set,
        // so if we are not 'here' then assume it's the pre-check.
        if (state.block === this) {
            val soil = world.getBlockState(pos.down())
            return soil.block.canSustainPlant(soil, world, pos.down(), EnumFacing.UP, this)
        }
        return canSustainBush(world.getBlockState(pos.down()))
    }

    override fun updateTick(worldIn: World, plantPos: BlockPos, state: IBlockState, rand: Random) {
        if (worldIn.isRemote) return
        checkAndDropBlock(worldIn, plantPos, state)

        val f = BlockCrops.getGrowthChance(this, worldIn, plantPos)
        var fruitPos = plantPos
        if (ForgeHooks.onCropsGrowPre(worldIn, plantPos, state, rand.nextInt((25.0f / f).toInt() + 1) == 0)) {
            if (!isMaxAge(state)) {
                worldIn.setBlockState(plantPos, state.withProperty(AGE, state.getValue(AGE) + 1), 2)
                worldIn.playEvent(1033, plantPos, 0)
            } else {
                for (facing in EnumFacing.Plane.HORIZONTAL)
                    if (worldIn.getBlockState(plantPos.offset(facing)).block === FLOWER) return

                fruitPos = plantPos.offset(EnumFacing.Plane.HORIZONTAL.random(rand))
                val soil = worldIn.getBlockState(fruitPos.down())

                // Rule E: a flower needs a source block of entropy fluid next to the stem. It is used up.
                val fluidPos = EnumFacing.Plane.HORIZONTAL.map { plantPos.offset(it) }.firstOrNull {
                    val fluid = worldIn.getBlockState(it)
                    fluid.block === ModBlocks.FLUID_ENTROPY_BLOCK && fluid.getValue(BlockFluidBase.LEVEL) == 0
                }

                if (fluidPos != null && worldIn.isAirBlock(fruitPos) &&
                    soil.block.canSustainPlant(soil, worldIn, fruitPos.down(), EnumFacing.UP, this)
                ) {
                    worldIn.setBlockToAir(fluidPos)
                    // The stem lives on: it stays at max age and grows the next flower after the old one is gone.
                    worldIn.setBlockState(fruitPos, FLOWER.defaultState)
                    worldIn.playEvent(1034, fruitPos, 0)
                }
            }
            ForgeHooks.onCropsGrowPost(worldIn, fruitPos, state, worldIn.getBlockState(fruitPos))
        }
    }

    override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(AGE, meta)

    override fun getMetaFromState(state: IBlockState): Int = state.getValue(AGE)

    override fun handleAssets() {
        super.handleAssets()
        EndReforked.getProxy().registerOrganaFlowerStemStateMapper(this)
    }

    companion object {
        @JvmField
        val AGE: PropertyInteger = PropertyInteger.create("age", 0, 9)

        @JvmField
        val FACING: PropertyDirection = BlockTorch.FACING

        @JvmField
        val STEM_AABB = Array(8) { AxisAlignedBB(0.375, 0.0, 0.375, 0.625, (it + 1) * 0.125, 0.625) }

        private val FLOWER: BlockOrganaFlower = ModBlocks.ORGANA_FLOWER_BLOCK
    }
}
