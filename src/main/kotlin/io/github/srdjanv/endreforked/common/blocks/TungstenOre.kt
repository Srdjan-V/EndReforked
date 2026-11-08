package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material

class TungstenOre(name: String, material: Material) : BlockBase(name, material) {

    init {
        setSoundType(SoundType.STONE)
        setHardness(4.0f)
        setResistance(25.0f)
        setHarvestLevel("pickaxe", 2)
    }
}
