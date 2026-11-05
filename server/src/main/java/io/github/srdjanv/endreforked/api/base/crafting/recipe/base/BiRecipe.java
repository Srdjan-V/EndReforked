package io.github.srdjanv.endreforked.api.base.crafting.recipe.base;

import java.util.function.BiFunction;

public abstract class BiRecipe<IN1, IN2, OUT> extends BaseRecipe<IN1, BiFunction<IN1, IN2, OUT>> {

    private final IN2 input2;

    public BiRecipe(IN1 input, IN2 input2, BiFunction<IN1, IN2, OUT> recipeFunction) {
        super(input, recipeFunction);
        this.input2 = input2;
    }

    public IN2 getInput2() {
        return input2;
    }
}
