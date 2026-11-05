package io.github.srdjanv.endreforked.api.fluids.entropy;

import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import io.github.srdjanv.endreforked.api.fluids.base.FluidCollisionHandler;
import io.github.srdjanv.endreforked.common.ModFluids;

public class EntropyFluidCollisionHandler extends FluidCollisionHandler {

    public static final EntropyFluidCollisionHandler INSTANCE = new EntropyFluidCollisionHandler();

    private EntropyFluidCollisionHandler() {
        super((IFluidInteractable) ModFluids.ENTROPY.get().getBlock());
    }
}
