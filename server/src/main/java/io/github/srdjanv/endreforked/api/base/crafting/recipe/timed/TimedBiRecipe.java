package io.github.srdjanv.endreforked.api.base.crafting.recipe.timed;

import java.util.function.BiFunction;

import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.BiRecipe;

public class TimedBiRecipe<IN1, IN2, OUT> extends BiRecipe<IN1, IN2, OUT> {

    protected final int ticksToComplete;

    public TimedBiRecipe(IN1 input, IN2 input2, int ticksToComplete, BiFunction<IN1, IN2, OUT> recipeFunction) {
        super(input, input2, recipeFunction);
        this.ticksToComplete = ticksToComplete;
    }

    public int getTicksToComplete() {
        return ticksToComplete;
    }
}
