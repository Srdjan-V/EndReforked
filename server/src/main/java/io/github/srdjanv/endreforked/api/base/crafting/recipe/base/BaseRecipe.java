package io.github.srdjanv.endreforked.api.base.crafting.recipe.base;

public abstract class BaseRecipe<IN, F> {

    protected final IN input;
    protected final F recipeFunction;

    public BaseRecipe(IN input, F recipeFunction) {
        this.input = input;
        this.recipeFunction = recipeFunction;
    }

    public IN getInput() {
        return input;
    }

    public F getRecipeFunction() {
        return recipeFunction;
    }
}
