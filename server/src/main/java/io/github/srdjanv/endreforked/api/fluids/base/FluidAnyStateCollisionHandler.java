package io.github.srdjanv.endreforked.api.fluids.base;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import io.github.srdjanv.endreforked.api.base.crafting.HandlerRegistry;
import io.github.srdjanv.endreforked.api.fluids.HashStrategies;
import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class FluidAnyStateCollisionHandler extends HandlerRegistry<Block, CollisionRecipe<Block, IBlockState>> {

    private static final List<FluidAnyStateCollisionHandler> HANDLERS = new ObjectArrayList<>();

    public static List<FluidAnyStateCollisionHandler> getHandlers() {
        return HANDLERS;
    }

    private final IFluidInteractable interactable;

    public FluidAnyStateCollisionHandler(IFluidInteractable interactable) {
        HANDLERS.add(this);
        this.interactable = interactable;
    }

    public IFluidInteractable getInteractable() {
        return interactable;
    }

    @Override
    public Hash.Strategy<Block> getHashStrategy() {
        return HashStrategies.FLUID_ANY_STATE_HASH_STRATEGY;
    }
}
