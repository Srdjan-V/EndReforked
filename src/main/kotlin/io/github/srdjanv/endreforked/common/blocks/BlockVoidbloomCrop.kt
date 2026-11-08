package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.blocks.base.endSoils
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockCrops
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import java.util.Random

class BlockVoidbloomCrop : BaseBlockCrops("voidbloom_crop", ::endSoils) {
    override fun createBlockState() = BlockStateContainer(this, AGE)

    override fun getAgeProperty() = AGE

    override fun getMaxAge() = 3

    private fun dropPearls(worldIn: World, pos: BlockPos) {
        worldIn.spawnEntity(
            EntityItem(worldIn, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), ItemStack(Items.ENDER_PEARL, worldIn.rand.nextInt(3)))
        )
    }

    override fun onBlockActivated(
        worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
        hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float,
    ): Boolean {
        if (worldIn.isRemote || !isMaxAge(state)) return false
        dropPearls(worldIn, pos)
        worldIn.setBlockState(pos, ModBlocks.VOIDBLOOM_CROP_WITHERED.defaultState)
        return true
    }

    override fun getSeed(): Item = ModItems.VOIDBLOOM_CROP

    override fun getCrop(): Item = Items.ENDER_PEARL

    override fun harvestBlock(
        worldIn: World, player: EntityPlayer, pos: BlockPos, state: IBlockState, te: TileEntity?, stack: ItemStack,
    ) {
        if (!worldIn.isRemote && isMaxAge(state)) dropPearls(worldIn, pos)
    }

    override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int) {
        if (getAge(state) >= getMaxAge()) drops.add(ItemStack(getCrop()))
    }

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = if (isMaxAge(state)) getCrop() else Items.AIR

    override fun quantityDropped(random: Random) = 1

    override fun canBlockStay(world: World, pos: BlockPos, state: IBlockState): Boolean {
        // Forge: called during world gen and placement, before this block is set, so if we are not 'here'
        // then assume it's the pre-check.
        if (state.block == this) {
            val soil = world.getBlockState(pos.down())
            return soil.block.canSustainPlant(soil, world, pos.down(), EnumFacing.UP, this)
        }
        return canSustainBush(world.getBlockState(pos.down()))
    }

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        if (worldIn.isRemote) return
        checkAndDropBlock(worldIn, pos, state)
        // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (!worldIn.isAreaLoaded(pos, 1)) return
        val age = getAge(state)
        if (age < getMaxAge()) {
            val chance = getGrowthChance(this, worldIn, pos)
            if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt((2.0F / chance).toInt() + 1) == 0)) {
                worldIn.setBlockState(pos, withAge(age + 1), 2)
                ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos))
            }
        }
    }

    companion object {
        @JvmField
        val AGE: PropertyInteger = PropertyInteger.create("age", 0, 3)
    }
}
