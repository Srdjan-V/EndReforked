package io.github.srdjanv.endreforked.api.fluids.organa;

import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import io.github.srdjanv.endreforked.api.fluids.base.FluidEntityCollisionHandler;
import io.github.srdjanv.endreforked.common.ModFluids;

public class OrganaFluidEntityCollisionHandler extends FluidEntityCollisionHandler {

    public static final OrganaFluidEntityCollisionHandler INSTANCE = new OrganaFluidEntityCollisionHandler();

    private OrganaFluidEntityCollisionHandler() {
        super((IFluidInteractable) ModFluids.ORGANA.get().getBlock());
    }
}
