package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.utils.randomTickPassiveInducer
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

/** The only passive source of entropy. Unbreakable, drops nothing, and generates in the Entropy biome only. */
class BlockEntropyRift : BlockBase("entropy_rift", Material.ROCK) {
    init {
        setSoundType(SoundType.STONE)
        setBlockUnbreakable()
        setResistance(6_000_000f)
        setTickRandomly(true)
    }

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = Items.AIR

    override fun quantityDropped(random: Random) = 0

    override fun randomTick(world: World, pos: BlockPos, state: IBlockState, random: Random) {
        randomTickPassiveInducer(world, pos, EntropyTuning.rift, 0x5900B3, EnumParticleTypes.PORTAL)
    }
}
