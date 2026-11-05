package io.github.srdjanv.endreforked.api.entropy.chamber;

import java.util.function.Function;

import net.minecraftforge.fluids.FluidStack;

public class FluidChamberRecipe extends ChamberRecipe<FluidStack, FluidStack> {

    public FluidChamberRecipe(FluidStack input, int ticksToComplete, int entropyCost,
                              Function<FluidStack, FluidStack> recipeFunction) {
        super(input, ticksToComplete, entropyCost, recipeFunction);
    }
}
