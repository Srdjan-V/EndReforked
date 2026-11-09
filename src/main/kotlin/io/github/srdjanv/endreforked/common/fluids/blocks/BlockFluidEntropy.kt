package io.github.srdjanv.endreforked.common.fluids.blocks

import io.github.srdjanv.endreforked.common.ModFluids
import io.github.srdjanv.endreforked.common.fluids.base.BaseBlockFluid
import io.github.srdjanv.endreforked.common.world.FluidInteractions
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
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

    override fun randomTick(world: World, pos: BlockPos, state: IBlockState, random: Random) {
        FluidInteractions.randomTick(world, pos, state, random, fluid)
    }
}
