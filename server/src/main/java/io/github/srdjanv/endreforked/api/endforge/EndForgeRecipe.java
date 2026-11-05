package io.github.srdjanv.endreforked.api.endforge;

import java.util.function.BiFunction;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import io.github.srdjanv.endreforked.api.base.crafting.recipe.timed.TimedBiRecipe;

public class EndForgeRecipe extends TimedBiRecipe<FluidStack, ItemStack, ItemStack> {

    public EndForgeRecipe(FluidStack input, ItemStack inpu2, int ticksToComplete,
                          BiFunction<FluidStack, ItemStack, ItemStack> recipeFunction) {
        super(input, inpu2, ticksToComplete, recipeFunction);
    }
}
