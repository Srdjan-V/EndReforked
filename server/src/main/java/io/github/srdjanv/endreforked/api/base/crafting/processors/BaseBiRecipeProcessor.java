package io.github.srdjanv.endreforked.api.base.crafting.processors;

import java.util.Objects;

import io.github.srdjanv.endreforked.api.base.crafting.HandlerGroupingRegistry;
import io.github.srdjanv.endreforked.api.base.crafting.groupings.RecipeGrouping;
import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.BiRecipe;

public abstract class BaseBiRecipeProcessor<IN1, IN2, OUT,
        RG extends RecipeGrouping<IN1, IN2, R>,
        R extends BiRecipe<IN1, IN2, OUT>> {

    protected final HandlerGroupingRegistry<IN1, IN2, OUT, RG, R> handlerGroupingRegistry;
    protected RG recipeGrouping;
    protected R recipe;

    public BaseBiRecipeProcessor(HandlerGroupingRegistry<IN1, IN2, OUT, RG, R> handlerGroupingRegistry) {
        this.handlerGroupingRegistry = handlerGroupingRegistry;
    }

    public boolean validateGrouping(IN1 input) {
        if (Objects.isNull(recipeGrouping)) {
            recipeGrouping = handlerGroupingRegistry.findRecipeGrouping(input);
            return Objects.nonNull(recipeGrouping);
        }
        if (handlerGroupingRegistry.getHashStrategy().equals(input, recipeGrouping.getGrouping())) return true;
        recipeGrouping = handlerGroupingRegistry.findRecipeGrouping(input);

        return Objects.nonNull(recipeGrouping);
    }

    public boolean validateRecipe(IN2 input) {
        if (Objects.isNull(recipeGrouping)) return false;
        if (Objects.isNull(recipe)) {
            recipe = handlerGroupingRegistry.findRecipe(recipeGrouping, input);
            return Objects.nonNull(recipe);
        }
        if (recipeGrouping.getHashStrategy().equals(input, recipe.getInput2())) return true;
        recipe = handlerGroupingRegistry.findRecipe(recipeGrouping, input);
        return Objects.nonNull(recipe);
    }

    public HandlerGroupingRegistry<IN1, IN2, OUT, RG, R> getHandlerRegistry() {
        return handlerGroupingRegistry;
    }

    public RG getRecipeGrouping() {
        return recipeGrouping;
    }

    public R getRecipe() {
        return recipe;
    }

    public boolean hasRecipeGroupingRecipe() {
        return Objects.nonNull(recipeGrouping);
    }

    public boolean hasRecipe() {
        return Objects.nonNull(recipe);
    }
}
