package io.github.srdjanv.endreforked.api.base.crafting.groupings;

import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.base.crafting.HashStrategyTranslator;
import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.BiRecipe;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public abstract class RecipeGrouping<IN1, IN2, R extends BiRecipe<IN1, IN2, ?>>
                                    implements HashStrategyTranslator<Hash.Strategy<IN2>> {

    private final IN1 grouping;
    private final Map<IN2, R> recipes;
    private final Hash.Strategy<IN2> hashStrategy;

    public RecipeGrouping(IN1 grouping, Hash.Strategy<IN2> hashStrategy) {
        this.grouping = grouping;
        this.hashStrategy = hashStrategy;
        this.recipes = new Object2ObjectOpenCustomHashMap<>(hashStrategy);
    }

    public R registerRecipe(Function<IN1, R> recipeFunction) {
        var recipe = recipeFunction.apply(grouping);
        recipes.put(recipe.getInput2(), recipe);
        return recipe;
    }

    @Nullable
    public R findRecipe(IN2 input) {
        return recipes.get(input);
    }

    public IN1 getGrouping() {
        return grouping;
    }

    public Map<IN2, R> getRecipes() {
        return recipes;
    }

    @Override
    public Hash.Strategy<IN2> getHashStrategy() {
        return hashStrategy;
    }
}
