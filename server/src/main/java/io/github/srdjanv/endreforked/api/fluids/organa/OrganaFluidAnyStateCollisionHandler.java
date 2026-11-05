package io.github.srdjanv.endreforked.api.fluids.organa;

import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import io.github.srdjanv.endreforked.api.fluids.base.FluidAnyStateCollisionHandler;
import io.github.srdjanv.endreforked.common.ModFluids;

public class OrganaFluidAnyStateCollisionHandler extends FluidAnyStateCollisionHandler {

    public static final OrganaFluidAnyStateCollisionHandler INSTANCE = new OrganaFluidAnyStateCollisionHandler();

    private OrganaFluidAnyStateCollisionHandler() {
        super((IFluidInteractable) ModFluids.ORGANA.get().getBlock());
    }
}
