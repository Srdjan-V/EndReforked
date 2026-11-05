package io.github.srdjanv.endreforked.api.entropy.chamber;

import java.util.function.Function;

import io.github.srdjanv.endreforked.api.base.crafting.EntropyRecipe;
import io.github.srdjanv.endreforked.api.base.crafting.recipe.timed.TimedRecipe;

public class ChamberRecipe<IN, OUT> extends TimedRecipe<IN, OUT> implements EntropyRecipe {

    private final int entropyCost;

    public ChamberRecipe(IN input, int ticksToComplete, int entropyCost, Function<IN, OUT> recipeFunction) {
        super(input, ticksToComplete, recipeFunction);
        this.entropyCost = entropyCost;
    }

    @Override
    public int getEntropyCost() {
        return entropyCost;
    }
}
