package io.github.srdjanv.endreforked.api.base.crafting.processors;

import java.util.Objects;

import io.github.srdjanv.endreforked.api.base.crafting.HandlerRegistry;
import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.Recipe;

public class BaseRecipeProcessor<IN, OUT, R extends Recipe<IN, OUT>> {

    protected final HandlerRegistry<IN, R> handlerRegistry;
    protected R recipe;

    public BaseRecipeProcessor(HandlerRegistry<IN, R> handlerRegistry) {
        this.handlerRegistry = handlerRegistry;
    }

    public boolean validateRecipe(IN input) {
        if (Objects.isNull(recipe)) {
            recipe = handlerRegistry.findRecipe(input);
            return Objects.nonNull(recipe);
        }
        if (handlerRegistry.getHashStrategy().equals(input, recipe.getInput())) return true;
        recipe = handlerRegistry.findRecipe(input);
        return Objects.nonNull(recipe);
    }

    public HandlerRegistry<IN, R> getHandlerRegistry() {
        return handlerRegistry;
    }

    public R getRecipe() {
        return recipe;
    }

    public boolean hasRecipe() {
        return Objects.nonNull(recipe);
    }
}
