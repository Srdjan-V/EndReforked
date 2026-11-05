package io.github.srdjanv.endreforked.common.fluids;

import net.minecraftforge.fluids.Fluid;

import io.github.srdjanv.endreforked.common.fluids.base.FluidTextures;

public class FluidOrgana extends Fluid {

    public static final int COLOUR = 0xffe580ff;

    public FluidOrgana() {
        super("organa",
                FluidTextures.STILL,
                FluidTextures.FLOWING,
                COLOUR);
    }
}
