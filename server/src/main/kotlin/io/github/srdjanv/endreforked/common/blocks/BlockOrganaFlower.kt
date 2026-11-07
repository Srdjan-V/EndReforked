package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.utils.randomTickPassiveInducer
import net.minecraft.block.Block
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.EnumFacing
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random
import kotlin.math.abs

class BlockOrganaFlower : BlockBase("organa_flower", Material.PLANTS, MapColor.PURPLE) {

    init {
        setTickRandomly(true)
        setDefaultState(blockState.baseState.withProperty(AGE, 0))
        setHarvestLevel("pickaxe", 2)
        setHardness(1.25f)
    }

    override fun handleAssets() {
        EndReforked.getProxy().registerItemRenderer(ModItems.ORGANA_FLOWER, 0, "inventory")
        EndReforked.getProxy().registerItemRenderer(ModItems.ORGANA_FLOWER, 10, "inventory")
    }

    override fun getSubBlocks(itemIn: CreativeTabs, items: NonNullList<ItemStack>) {
        items.add(ItemStack(this, 1, 0))
        items.add(ItemStack(this, 1, 8))
        items.add(ItemStack(this, 1, 10))
    }

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = Items.AIR

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        if (worldIn.isRemote) return
        if (!canSurvive(worldIn, pos)) {
            worldIn.destroyBlock(pos, true)
            return
        }
        val upPos = pos.up()

        if (!worldIn.isAirBlock(upPos) || upPos.y >= 256) return
        val age = state.getValue(AGE)

        if (age >= 10 || !ForgeHooks.onCropsGrowPre(worldIn, upPos, state, true)) return
        if (age >= 8) {
            when (age) {
                8 -> if (rand.nextDouble() < 0.2) worldIn.setBlockState(pos, defaultState.withProperty(AGE, 9), 2)
                9 -> placeBigDeadFlower(worldIn, pos)
            }
            return
        }

        var growOnlyUp = false
        var flag1 = false
        val downBlockState = worldIn.getBlockState(pos.down())
        val downBlock = downBlockState.block

        if (downBlock === ModBlocks.END_MOSS_GRASS_BLOCK) {
            growOnlyUp = true
        } else if (downBlock === ModBlocks.ORGANA_PLANT_BLOCK) {
            var j = 1
            for (k in 0 until 4) {
                val block1 = worldIn.getBlockState(pos.down(j + 1)).block
                if (block1 !== ModBlocks.ORGANA_PLANT_BLOCK) {
                    if (block1 === ModBlocks.END_MOSS_GRASS_BLOCK) flag1 = true
                    break
                }
                ++j
            }

            var i1 = 4
            if (flag1) ++i1
            if (j < 2 || rand.nextInt(i1) >= j) growOnlyUp = true
        } else if (downBlockState.material === Material.AIR) growOnlyUp = true

        if (growOnlyUp && areAllNeighborsEmpty(worldIn, upPos, null) && worldIn.isAirBlock(pos.up(2))) {
            placePlant(worldIn, pos)
            placeGrownFlower(worldIn, upPos, age + 1)
        } else if (age < 7) {
            var l = rand.nextInt(4)
            var flag2 = false

            if (flag1) ++l
            for (j1 in 0 until l) {
                val facing = EnumFacing.Plane.HORIZONTAL.random(rand)
                val facingPos = pos.offset(facing)

                if (worldIn.isAirBlock(facingPos) && worldIn.isAirBlock(facingPos.down()) &&
                    areAllNeighborsEmpty(worldIn, facingPos, facing.opposite)
                ) {
                    placeGrownFlower(worldIn, facingPos, age + 1)
                    flag2 = true
                }
            }

            if (flag2) placePlant(worldIn, pos) else placeSmallDeadFlower(worldIn, pos)
        } else placeSmallDeadFlower(worldIn, pos)
        ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos))
    }

    private fun placePlant(worldIn: World, pos: BlockPos) {
        worldIn.setBlockState(pos, ModBlocks.ORGANA_PLANT_BLOCK.defaultState, 2)
    }

    private fun placeGrownFlower(worldIn: World, pos: BlockPos, age: Int) {
        worldIn.setBlockState(pos, defaultState.withProperty(AGE, age), 2)
        worldIn.playEvent(1033, pos, 0)
    }

    private fun placeBigDeadFlower(worldIn: World, pos: BlockPos) {
        worldIn.setBlockState(pos, defaultState.withProperty(AGE, 10), 2)
        worldIn.playEvent(1034, pos, 0)
    }

    private fun placeSmallDeadFlower(worldIn: World, pos: BlockPos) {
        worldIn.setBlockState(pos, defaultState.withProperty(AGE, 8), 2)
        worldIn.playEvent(1034, pos, 0)
    }

    override fun isFullCube(state: IBlockState) = false

    override fun isOpaqueCube(state: IBlockState) = false

    override fun canPlaceBlockAt(worldIn: World, pos: BlockPos) =
        super.canPlaceBlockAt(worldIn, pos) && canSurvive(worldIn, pos)

    override fun neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos) {
        if (!canSurvive(worldIn, pos)) worldIn.scheduleUpdate(pos, this, 1)
    }

    fun canSurvive(worldIn: World, pos: BlockPos): Boolean {
        val belowState = worldIn.getBlockState(pos.down())
        val belowBlock = belowState.block

        if (belowBlock === ModBlocks.ORGANA_PLANT_BLOCK || belowBlock === ModBlocks.END_MOSS_GRASS_BLOCK)
            return true
        if (belowState.material !== Material.AIR) return false
        var i = 0

        for (enumfacing in EnumFacing.Plane.HORIZONTAL) {
            val facingState = worldIn.getBlockState(pos.offset(enumfacing))
            if (facingState.block === ModBlocks.ORGANA_PLANT_BLOCK) {
                ++i
            } else if (facingState.material !== Material.AIR) {
                return false
            }
        }

        return i == 1
    }

    override fun harvestBlock(
        worldIn: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, te: TileEntity?, stack: ItemStack,
    ) {
        super.harvestBlock(worldIn, player, pos, state, te, stack)
        spawnAsEntity(worldIn, pos, ItemStack(Item.getItemFromBlock(this)))
    }

    override fun getSilkTouchDrop(state: IBlockState): ItemStack = ItemStack.EMPTY

    @SideOnly(Side.CLIENT)
    override fun getRenderLayer() = BlockRenderLayer.CUTOUT

    override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(AGE, meta)

    override fun getMetaFromState(state: IBlockState): Int = state.getValue(AGE)

    override fun createBlockState() = BlockStateContainer(this, AGE)

    override fun getBlockFaceShape(worldIn: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) =
        BlockFaceShape.UNDEFINED

    override fun tickRate(world: World): Int {
        return EntropyTuning.organaFlower.interval
    }

    override fun randomTick(world: World, pos: BlockPos, state: IBlockState, random: Random) {
        if (state.getValue(AGE) == 10) {
            randomTickPassiveInducer(world, pos, EntropyTuning.organaFlower, 0x5900B3, EnumParticleTypes.SPELL_MOB)
        }
    }

    companion object {
        @JvmField
        val AGE: PropertyInteger = PropertyInteger.create("age", 0, 10)

        private fun areAllNeighborsEmpty(worldIn: World, pos: BlockPos, excludingSide: EnumFacing?) =
            EnumFacing.Plane.HORIZONTAL.none { it != excludingSide && !worldIn.isAirBlock(pos.offset(it)) }

        @JvmStatic
        fun generatePlant(worldIn: World, pos: BlockPos, rand: Random, plantSpace: Int) {
            worldIn.setBlockState(pos, ModBlocks.ORGANA_PLANT_BLOCK.defaultState, 2)
            growPlantRecursive(worldIn, pos, rand, pos, plantSpace, 0)
        }

        private fun growPlantRecursive(
            worldIn: World, pos: BlockPos, rand: Random, pos2: BlockPos, plantSpace: Int, iteration: Int,
        ) {
            var height = rand.nextInt(3) + 1
            if (iteration == 0) ++height

            for (j in 0 until height) {
                val upPos = pos.up(j + 1)
                if (!areAllNeighborsEmpty(worldIn, upPos, null)) {
                    height = j
                    break
                }
                worldIn.setBlockState(upPos, ModBlocks.ORGANA_PLANT_BLOCK.defaultState, 2)
            }

            var grown = false
            if (iteration < 4) {
                var bend = rand.nextInt(4)
                if (iteration == 0) ++bend

                for (k in 0 until bend) {
                    val enumfacing = EnumFacing.Plane.HORIZONTAL.random(rand)
                    val genPos = pos.up(height).offset(enumfacing)

                    if (abs(genPos.x - pos2.x) < plantSpace && abs(genPos.z - pos2.z) < plantSpace &&
                        worldIn.isAirBlock(genPos) && worldIn.isAirBlock(genPos.down()) &&
                        areAllNeighborsEmpty(worldIn, genPos, enumfacing.opposite)
                    ) {
                        grown = true
                        worldIn.setBlockState(genPos, ModBlocks.ORGANA_PLANT_BLOCK.defaultState, 2)
                        growPlantRecursive(worldIn, genPos, rand, pos2, plantSpace, iteration + 1)
                    }
                }
            }

            if (!grown) {
                worldIn.setBlockState(
                    pos.up(height),
                    ModBlocks.ORGANA_FLOWER_BLOCK.defaultState.withProperty(AGE, 10), 2,
                )
            }
        }
    }
}
