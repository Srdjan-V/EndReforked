package io.github.srdjanv.endreforked.api.base.crafting.recipe.timed;

import java.util.function.Function;

import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.Recipe;

public class TimedRecipe<IN, OUT> extends Recipe<IN, OUT> {

    protected final int ticksToComplete;

    public TimedRecipe(IN input, int ticksToComplete, Function<IN, OUT> recipeFunction) {
        super(input, recipeFunction);
        this.ticksToComplete = ticksToComplete;
    }

    public int getTicksToComplete() {
        return ticksToComplete;
    }
}
