package io.github.srdjanv.endreforked.common.blocks.base

import net.minecraft.block.SoundType
import net.minecraft.block.material.Material

open class BlockEndBase(name: String, material: Material) : BlockBase(name, material) {
    init {
        setSoundType(SoundType.STONE)
        setHardness(3.0F)
        setHarvestLevel("pickaxe", 2)
    }
}
