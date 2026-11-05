package io.github.srdjanv.endreforked.common.fluids;

import net.minecraftforge.fluids.Fluid;

import io.github.srdjanv.endreforked.common.fluids.base.FluidTextures;

public class FluidEndMagma extends Fluid {

    public static final int COLOUR = 0xff321782;

    public FluidEndMagma() {
        super("end_magma",
                FluidTextures.MOLTEN_STILL,
                FluidTextures.MOLTEN_FLOWING,
                COLOUR);

        setLuminosity(15).setDensity(3500).setViscosity(4500).setTemperature(1600);
        // setBlock(ModBlocks.BLOCK_FLUID_END_MAGMA.get());
    }
}
