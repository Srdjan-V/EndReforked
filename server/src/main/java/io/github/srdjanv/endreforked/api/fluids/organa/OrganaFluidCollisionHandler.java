package io.github.srdjanv.endreforked.api.fluids.organa;

import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import io.github.srdjanv.endreforked.api.fluids.base.FluidCollisionHandler;
import io.github.srdjanv.endreforked.common.ModFluids;

public class OrganaFluidCollisionHandler extends FluidCollisionHandler {

    public static final OrganaFluidCollisionHandler INSTANCE = new OrganaFluidCollisionHandler();

    private OrganaFluidCollisionHandler() {
        super((IFluidInteractable) ModFluids.ORGANA.get().getBlock());
    }
}
