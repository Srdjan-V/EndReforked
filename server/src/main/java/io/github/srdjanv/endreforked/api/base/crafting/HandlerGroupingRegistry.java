package io.github.srdjanv.endreforked.api.base.crafting;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.base.crafting.groupings.RecipeGrouping;
import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.BiRecipe;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public abstract class HandlerGroupingRegistry<IN1, IN2, OUT,
        RG extends RecipeGrouping<IN1, IN2, R>,
        R extends BiRecipe<IN1, IN2, OUT>>
                                             implements HashStrategyTranslator<Hash.Strategy<IN1>> {

    public abstract Hash.Strategy<IN1> getHashStrategy();

    public final Map<IN1, RG> registry = new Object2ObjectOpenCustomHashMap<>(getHashStrategy());

    public Map<IN1, RG> getRegistry() {
        return registry;
    }

    public Collection<RG> getRecipeGroupings() {
        return registry.values();
    }

    public boolean registerRecipeGrouping(IN1 target, RG grouping) {
        registry.put(target, grouping);
        return true;
    }

    public boolean registerRecipeGrouping(RG grouping) {
        registry.put(grouping.getGrouping(), grouping);
        return true;
    }

    @Nullable
    public RG findRecipeGrouping(IN1 input) {
        return registry.get(input);
    }

    public boolean registerRecipeToGrouping(RG grouping, Function<IN1, R> recipeFunction) {
        grouping.registerRecipe(recipeFunction);
        return true;
    }

    @Nullable
    public R findRecipe(RG grouping, IN2 input) {
        return grouping.findRecipe(input);
    }
}
