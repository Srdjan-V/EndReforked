package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import java.util.Random

/** Organa soil. It is not an inducer (rule A). */
class BlockOrganaEndStone : BlockBase("organa_end_stone_block", Material.ROCK, MapColor.PURPLE) {
    init {
        setSoundType(SoundType.STONE)
        setHarvestLevel("pickaxe", 3)
        setHardness(1.5F)
    }

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ModItems.ORGANA_END_STONE_BLOCK
}
