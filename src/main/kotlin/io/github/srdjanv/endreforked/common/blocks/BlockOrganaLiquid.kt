package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.entity.EntityChronologist
import io.github.srdjanv.endreforked.common.entity.EntityWatcher
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockOrganaLiquid : BlockBase("organa_liquid", Material.ROCK) {
    init {
        setTickRandomly(true)
        setSoundType(SoundType.STONE)
        setHardness(3.0F)
        setHarvestLevel("pickaxe", 2)
    }

    override fun getMapColor(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): MapColor = MapColor.PURPLE

    override fun onEntityWalk(worldIn: World, pos: BlockPos, entityIn: Entity) {
        if (entityIn !is EntityEnderman && entityIn !is EntityWatcher && entityIn !is EntityChronologist &&
            !entityIn.isImmuneToFire && entityIn is EntityLivingBase &&
            !EnchantmentHelper.hasFrostWalkerEnchantment(entityIn)
        ) {
            entityIn.attackEntityFrom(DamageSource.HOT_FLOOR, 1.0F)
        }
        super.onEntityWalk(worldIn, pos, entityIn)
    }

    @SideOnly(Side.CLIENT)
    override fun getPackedLightmapCoords(state: IBlockState, source: IBlockAccess, pos: BlockPos) = 15728880

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        val above = pos.up()
        val block = worldIn.getBlockState(above).block
        if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
            worldIn.setBlockToAir(above)
            worldIn.playSound(
                null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
                2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F,
            )
            (worldIn as? WorldServer)?.spawnParticle(
                EnumParticleTypes.SMOKE_LARGE, above.x + 0.5, above.y + 0.25, above.z + 0.5,
                8, 0.5, 0.25, 0.5, 0.0,
            )
        }
    }

    override fun canEntitySpawn(state: IBlockState, entityIn: Entity) = entityIn.isImmuneToFire
}
