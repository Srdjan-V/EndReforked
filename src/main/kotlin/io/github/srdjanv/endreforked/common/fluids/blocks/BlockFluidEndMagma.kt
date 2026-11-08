package io.github.srdjanv.endreforked.common.fluids.blocks

import io.github.srdjanv.endreforked.common.ModFluids
import io.github.srdjanv.endreforked.common.fluids.base.BaseBlockFluid
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material

class BlockFluidEndMagma : BaseBlockFluid("end_magma", ModFluids.END_MAGMA, Material.LAVA, MapColor.RED) {

    init {
        setHardness(100.0f).setLightLevel(1.0f)
        enableStats = false
    }
}
