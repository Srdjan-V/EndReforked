package io.github.srdjanv.endreforked.api.entropy.chamber;

import io.github.srdjanv.endreforked.api.base.crafting.HandlerRegistry;
import io.github.srdjanv.endreforked.api.base.crafting.recipe.timed.TimedRecipe;

public abstract class EntropyChamberHandler<IN, R extends TimedRecipe<IN, ?>> extends HandlerRegistry<IN, R> {}
