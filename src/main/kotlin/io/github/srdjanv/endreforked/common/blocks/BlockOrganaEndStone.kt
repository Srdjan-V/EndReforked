package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Optional
import java.util.Random

class BlockOrganaEndStone : BlockBase("organa_end_stone_block", Material.ROCK, MapColor.PURPLE), EntropyDataProvider {
    init {
        setSoundType(SoundType.STONE)
        setHarvestLevel("pickaxe", 3)
        setHardness(1.5F)
        setTickRandomly(true)
    }

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ModItems.ORGANA_END_STONE_BLOCK

    override val entropyRadius: Optional<EntropyRadius> = Optional.of(EntropyRadius.ONE)

    override val passiveInducer: Optional<EntropyDataProvider.PassiveInducer> = Optional.of(ONE_ENTROPY)

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        EntropyWorldHandler.getEntropyChunkWorld(worldIn.provider.dimension, pos)
            .ifPresent { it.induceEntropy(ONE_ENTROPY.induced, false) }
    }
}
