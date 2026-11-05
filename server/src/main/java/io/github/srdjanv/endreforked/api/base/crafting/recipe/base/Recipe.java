package io.github.srdjanv.endreforked.api.base.crafting.recipe.base;

import java.util.function.Function;

public abstract class Recipe<IN, OUT> extends BaseRecipe<IN, Function<IN, OUT>> {

    public Recipe(IN input, Function<IN, OUT> recipeFunction) {
        super(input, recipeFunction);
    }
}
