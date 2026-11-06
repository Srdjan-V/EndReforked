package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import net.minecraft.block.material.Material

class BlockPurpurLamp(name: String) : BlockBase(name, Material.ROCK) {

    init {
        setLightLevel(1.0f)
    }
}
