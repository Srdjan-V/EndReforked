package io.github.srdjanv.endreforked.common.fluids.blocks

import io.github.srdjanv.endreforked.common.ModFluids
import io.github.srdjanv.endreforked.common.fluids.base.BaseBlockFluid
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.FluidOrganaTile
import io.github.srdjanv.endreforked.common.world.FluidInteractions
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.potion.PotionEffect
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class BlockFluidOrgana : BaseBlockFluid("organa", ModFluids.ORGANA.get(), Material.WATER, MapColor.PURPLE) {

    init {
        setTickRandomly(true)
        setTickRate(10)
        setHardness(800f)
        setLightOpacity(2)
        enableStats = false
    }

    override fun hasTileEntity(state: IBlockState) = state.getValue(LEVEL) == 0

    override fun createTileEntity(world: World, state: IBlockState): TileEntity? =
        if (state.getValue(LEVEL) == 0) FluidOrganaTile() else null

    override fun onEntityCollision(worldIn: World, pos: BlockPos, state: IBlockState, entityIn: Entity) {
        FluidInteractions.entityCollision(worldIn, pos, state, entityIn, fluid)
        if (worldIn.isRemote) return
        entityIn.fallDistance = 0f
        if (entityIn is EntityPlayer) {
            if (entityIn.isSneaking) return
            entityIn.addPotionEffect(PotionEffect(MobEffects.LEVITATION, 40, 2))
            entityIn.addPotionEffect(PotionEffect(MobEffects.SPEED, 120, 2))
        } else if (entityIn is EntityLivingBase) {
            entityIn.addPotionEffect(PotionEffect(MobEffects.LEVITATION, 40))
            entityIn.addPotionEffect(PotionEffect(MobEffects.SPEED, 60))
        }
    }

    override fun randomTick(world: World, pos: BlockPos, state: IBlockState, random: Random) {
        FluidInteractions.randomTick(world, pos, state, random, fluid)
    }
}
