package io.github.srdjanv.endreforked.api.fluids.entropy;

import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import io.github.srdjanv.endreforked.api.fluids.base.FluidEntityCollisionHandler;
import io.github.srdjanv.endreforked.common.ModFluids;

public class EntropyFluidEntityCollisionHandler extends FluidEntityCollisionHandler {

    public static final EntropyFluidEntityCollisionHandler INSTANCE = new EntropyFluidEntityCollisionHandler();

    private EntropyFluidEntityCollisionHandler() {
        super((IFluidInteractable) ModFluids.ENTROPY.get().getBlock());
    }
}
