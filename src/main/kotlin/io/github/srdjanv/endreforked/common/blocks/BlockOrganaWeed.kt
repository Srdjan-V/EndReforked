package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockBush
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.utils.randomTickPassiveInducer
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.IShearable
import java.util.*

class BlockOrganaWeed : BaseBlockBush("organa_weed", Material.VINE, { setOf(ModBlocks.ORGANA_BLOCK) }),
    IShearable {

    init {
        tickRandomly = true
    }

    override fun getStateForPlacement(
        p_180642_1_: World?,
        p_180642_2_: BlockPos?,
        p_180642_3_: EnumFacing?,
        p_180642_4_: Float,
        p_180642_5_: Float,
        p_180642_6_: Float,
        p_180642_7_: Int,
        p_180642_8_: EntityLivingBase?
    ): IBlockState? {
        return super.getStateForPlacement(
            p_180642_1_,
            p_180642_2_,
            p_180642_3_,
            p_180642_4_,
            p_180642_5_,
            p_180642_6_,
            p_180642_7_,
            p_180642_8_
        )
    }

    override fun tickRate(world: World): Int {
        return EntropyTuning.organaWeed.interval
    }

    override fun randomTick(worldIn: World, pos: BlockPos, state: IBlockState, random: Random) {
        randomTickPassiveInducer(
            worldIn,
            pos,
            EntropyTuning.organaWeed,
            0x9B30FF,
            EnumParticleTypes.SPELL_MOB,
        )
    }

    override fun onEntityCollision(worldIn: World, pos: BlockPos, state: IBlockState, entityIn: Entity) {
        if (worldIn.isRemote) return
        entityIn.fallDistance = 0f
        if (entityIn is EntityPlayer) {
            if (entityIn.isSneaking) return
            entityIn.addPotionEffect(PotionEffect(MobEffects.LEVITATION, 20, 2))
            entityIn.addPotionEffect(PotionEffect(MobEffects.SPEED, 80, 2))
        } else if (entityIn is EntityLivingBase) {
            entityIn.addPotionEffect(PotionEffect(MobEffects.LEVITATION, 10))
            entityIn.addPotionEffect(PotionEffect(MobEffects.SPEED, 20))
        }
    }

    override fun getMapColor(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): MapColor = MapColor.PURPLE

    override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB =
        BlockEndCoral.END_BUSH_AABB

    override fun isReplaceable(worldIn: IBlockAccess, pos: BlockPos) = true

    override fun quantityDropped(random: Random) = if (random.nextInt(100) < 20) 1 else 0

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ModItems.ORGANA_FLOWER_SEED

    override fun isShearable(item: ItemStack, world: IBlockAccess, pos: BlockPos) = true

    override fun onSheared(item: ItemStack, world: IBlockAccess, pos: BlockPos, fortune: Int): List<ItemStack> =
        listOf(ItemStack(ModBlocks.ORGANA_WEED_BLOCK))
}
