package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Optional
import java.util.OptionalInt
import java.util.Random

/** Shared by [BlockEntropyEndStone] and [BlockEndMoss]. */
internal val ONE_ENTROPY = object : EntropyDataProvider.PassiveInducer {
    override val frequency: OptionalInt = OptionalInt.empty()
    override val induced = 1
}

class BlockEntropyEndStone : BlockBase("entropy_end_stone", Material.ROCK), EntropyDataProvider {
    init {
        setSoundType(SoundType.STONE)
        setHardness(3.0F)
        setHarvestLevel("pickaxe", 2)
        setTickRandomly(true)
    }

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ModItems.END_MOSS_BLOCK.get()

    override val entropyRadius: Optional<EntropyRadius> = Optional.of(EntropyRadius.ONE)

    override val passiveInducer: Optional<EntropyDataProvider.PassiveInducer> = Optional.of(ONE_ENTROPY)

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        EntropyWorldHandler.getEntropyChunkWorld(worldIn.provider.dimension, pos)
            .ifPresent { it.induceEntropy(ONE_ENTROPY.induced, false) }
    }
}
