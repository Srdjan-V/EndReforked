package io.github.srdjanv.endreforked.common.fluids

import io.github.srdjanv.endreforked.common.fluids.base.FluidTextures
import net.minecraftforge.fluids.Fluid

class FluidIchor : Fluid("ichor", FluidTextures.MOLTEN_STILL, FluidTextures.MOLTEN_FLOWING, COLOUR) {

    init {
        setLuminosity(15).setDensity(3500).setViscosity(4500).setTemperature(1600)
    }

    companion object {
        const val COLOUR = 0xff321782.toInt()
    }
}
