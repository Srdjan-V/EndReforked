package io.github.srdjanv.endreforked.api.base.crafting.groupings;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.BiRecipe;
import io.github.srdjanv.endreforked.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.Hash;

public class Fluid2ItemGrouping<R extends BiRecipe<FluidStack, ItemStack, ?>>
                               extends RecipeGrouping<FluidStack, ItemStack, R> {

    public Fluid2ItemGrouping(FluidStack grouping, Hash.Strategy<ItemStack> strategy) {
        super(grouping, strategy);
    }

    public Fluid2ItemGrouping(FluidStack grouping) {
        super(grouping, ItemStackHashStrategy.memorizedComparingAllButCount());
    }
}
