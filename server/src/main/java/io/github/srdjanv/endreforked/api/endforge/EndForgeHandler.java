package io.github.srdjanv.endreforked.api.endforge;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import io.github.srdjanv.endreforked.api.base.crafting.HandlerGroupingRegistry;
import io.github.srdjanv.endreforked.api.base.crafting.groupings.Fluid2ItemGrouping;
import io.github.srdjanv.endreforked.api.util.FluidStackHashStrategy;

public class EndForgeHandler extends
                             HandlerGroupingRegistry<FluidStack, ItemStack, ItemStack, Fluid2ItemGrouping<EndForgeRecipe>, EndForgeRecipe> {

    private static final EndForgeHandler instance = new EndForgeHandler();

    public static EndForgeHandler getInstance() {
        return instance;
    }

    @Override
    public FluidStackHashStrategy getHashStrategy() {
        return FluidStackHashStrategy.memorizedComparingAll();
    }
}
