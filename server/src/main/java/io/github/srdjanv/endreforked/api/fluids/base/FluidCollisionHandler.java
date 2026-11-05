package io.github.srdjanv.endreforked.api.fluids.base;

import java.util.List;

import net.minecraft.block.state.IBlockState;

import io.github.srdjanv.endreforked.api.base.crafting.HandlerRegistry;
import io.github.srdjanv.endreforked.api.fluids.HashStrategies;
import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FluidCollisionHandler extends HandlerRegistry<IBlockState, CollisionRecipe<IBlockState, IBlockState>> {

    private static final List<FluidCollisionHandler> HANDLERS = new ObjectArrayList<>();

    public static List<FluidCollisionHandler> getHandlers() {
        return HANDLERS;
    }

    private final IFluidInteractable interactable;

    public FluidCollisionHandler(IFluidInteractable interactable) {
        HANDLERS.add(this);
        this.interactable = interactable;
    }

    public IFluidInteractable getInteractable() {
        return interactable;
    }

    @Override
    public Hash.Strategy<IBlockState> getHashStrategy() {
        return HashStrategies.FLUID_SATE_HASH_STRATEGY;
    }
}
