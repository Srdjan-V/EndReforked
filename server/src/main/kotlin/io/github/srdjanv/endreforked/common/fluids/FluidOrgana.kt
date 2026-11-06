package io.github.srdjanv.endreforked.common.fluids

import io.github.srdjanv.endreforked.common.fluids.base.FluidTextures
import net.minecraftforge.fluids.Fluid

class FluidOrgana : Fluid("organa", FluidTextures.STILL, FluidTextures.FLOWING, COLOUR) {
    companion object {
        const val COLOUR = 0xffe580ff.toInt()
    }
}
