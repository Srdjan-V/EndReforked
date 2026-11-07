package io.github.srdjanv.endreforked.common.fluids.blocks

import io.github.srdjanv.endreforked.common.ModFluids
import io.github.srdjanv.endreforked.common.fluids.FluidOrgana
import io.github.srdjanv.endreforked.common.fluids.base.BaseBlockFluid
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.world.FluidInteractions
import io.github.srdjanv.endreforked.utils.randomTickPassiveInducer
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.world.World
import java.util.Random

class BlockFluidEntropy : BaseBlockFluid("entropy", ModFluids.ENTROPY, Material.WATER, MapColor.PURPLE) {

    init {
        setTickRandomly(true)
        setQuantaPerBlock(6)
        setTickRate(10)
        setHardness(1000.0f)
        setLightOpacity(1)
        enableStats = false
    }

    override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entityIn: Entity) {
        FluidInteractions.entityCollision(world, pos, state, entityIn, fluid)
    }

    override fun tickRate(world: World): Int {
        return EntropyTuning.fluidEntropy.interval
    }

    override fun randomTick(world: World, pos: BlockPos, state: IBlockState, random: Random) {
        FluidInteractions.randomTick(world, pos, state, random, fluid)
        if (state.getValue(LEVEL) == 0) {
            randomTickPassiveInducer(world, pos, EntropyTuning.fluidEntropy, FluidOrgana.COLOUR, EnumParticleTypes.SPELL)
        }
    }
}
