package io.github.srdjanv.endreforked.api.fluids.entropy;

import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import io.github.srdjanv.endreforked.api.fluids.base.FluidAnyStateCollisionHandler;
import io.github.srdjanv.endreforked.common.ModFluids;

public class EntropyFluidAnyStateCollisionHandler extends FluidAnyStateCollisionHandler {

    public static final EntropyFluidAnyStateCollisionHandler INSTANCE = new EntropyFluidAnyStateCollisionHandler();

    private EntropyFluidAnyStateCollisionHandler() {
        super((IFluidInteractable) ModFluids.ENTROPY.get().getBlock());
    }
}
