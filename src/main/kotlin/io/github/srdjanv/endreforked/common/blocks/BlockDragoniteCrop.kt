package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.blocks.base.endSoils
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockCrops
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.ForgeHooks
import java.util.Random

class BlockDragoniteCrop : BaseBlockCrops("dragonite_crop", ::endSoils) {
    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        checkAndDropBlock(worldIn, pos, state)
        val age = getAge(state)
        if (age >= getMaxAge()) return
        val chance = getGrowthChance(this, worldIn, pos)
        if (ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt((25.0F / chance).toInt() + 1) == 0)) {
            worldIn.setBlockState(pos, withAge(age + 1), 2)
            ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos))
        }
    }

    override fun createBlockState() = BlockStateContainer(this, AGE)

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos) =
        CROP_AABB[state.getValue(getAgeProperty())]

    override fun getAgeProperty() = AGE

    override fun getMaxAge() = 4

    override fun onBlockActivated(
        worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer,
        hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float,
    ): Boolean {
        if (worldIn.isRemote || !isMaxAge(state)) return false
        spawnAsEntity(worldIn, pos, ItemStack(ModItems.DRAGONITE_BERRIES, 2))
        worldIn.setBlockState(pos, withAge(0))
        return true
    }

    override fun getSeed(): Item = ModItems.DRAGONITE_BERRIES

    override fun getCrop(): Item = ModItems.DRAGONITE_BERRIES

    companion object {
        @JvmField
        val AGE: PropertyInteger = PropertyInteger.create("age", 0, 4)

        @JvmField
        val CROP_AABB = arrayOf(
            AxisAlignedBB(0.2, 0.0, 0.2, 0.8, 0.55, 0.8),
            AxisAlignedBB(0.2, 0.0, 0.2, 0.8, 0.70, 0.8),
            AxisAlignedBB(0.2, 0.0, 0.2, 0.8, 0.85, 0.8),
            AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 0.90, 0.9),
            AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 0.95, 0.9),
        )
    }
}
