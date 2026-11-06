package io.github.srdjanv.endreforked.common.fluids

import io.github.srdjanv.endreforked.common.fluids.base.BaseFluid
import io.github.srdjanv.endreforked.common.fluids.base.FluidTextures

class FluidEntropy : BaseFluid("entropy", FluidTextures.STILL, FluidTextures.FLOWING, COLOUR) {
    companion object {
        const val COLOUR = 0xff321782.toInt()
    }
}
