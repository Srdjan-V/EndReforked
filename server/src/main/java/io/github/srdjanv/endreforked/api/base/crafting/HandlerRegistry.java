package io.github.srdjanv.endreforked.api.base.crafting;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.BaseRecipe;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public abstract class HandlerRegistry<IN, R extends BaseRecipe<IN, ?>>
                                     implements HashStrategyTranslator<Hash.Strategy<IN>> {

    public abstract Hash.Strategy<IN> getHashStrategy();

    protected HandlerRegistry() {}

    protected final Map<IN, R> registry = new Object2ObjectOpenCustomHashMap<>(getHashStrategy());

    public Map<IN, R> getRegistry() {
        return registry;
    }

    public boolean registerRecipe(R recipe) {
        registry.put(recipe.getInput(), recipe);
        return true;
    }

    @Nullable
    public R findRecipe(IN input) {
        return registry.get(input);
    }
}
