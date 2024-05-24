package io.github.srdjanv.endreforked.api.fluids.base;

import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.Recipe;
import io.github.srdjanv.endreforked.api.fluids.IWorldRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class CollisionRecipe<IN, OUT> extends Recipe<IN, OUT> implements IWorldRecipe {
    public static final BiConsumer<WorldServer, BlockPos> EMPTY_INTERACTION_CALLBACK = (world, pos) -> {};
    public static final BiConsumer<WorldServer, BlockPos> EMPTY_FLUID_INTERACTION_CALLBACK = (world, pos) -> {};


    protected final int chance;
    protected final boolean consumeSource;
    protected final BiConsumer<WorldServer, BlockPos> interactionCallback;
    protected final BiConsumer<WorldServer, BlockPos> fluidInteractionCallback;

    public CollisionRecipe(IN input, int chance, boolean consumeSource, Function<IN, OUT> recipeFunction) {
        this(input, chance, consumeSource, recipeFunction, EMPTY_FLUID_INTERACTION_CALLBACK, EMPTY_INTERACTION_CALLBACK);
    }

    public CollisionRecipe(IN input, int chance, boolean consumeSource, Function<IN, OUT> recipeFunction,
                           BiConsumer<WorldServer, BlockPos> interactionCallback,
                           BiConsumer<WorldServer, BlockPos> fluidInteractionCallback) {
        super(input, recipeFunction);
        this.chance = chance;
        this.consumeSource = consumeSource;
        this.interactionCallback = interactionCallback;
        this.fluidInteractionCallback = fluidInteractionCallback;
    }

    @Override
    public int getChance() {
        return chance;
    }

    @Override
    public boolean isConsumeSource() {
        return consumeSource;
    }

    @Override
    public BiConsumer<WorldServer, BlockPos> getFluidInteractionCallback() {
        return fluidInteractionCallback;
    }

    @Override
    public BiConsumer<WorldServer, BlockPos> getInteractionCallback() {
        return interactionCallback;
    }
}