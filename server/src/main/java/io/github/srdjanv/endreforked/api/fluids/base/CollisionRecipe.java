package io.github.srdjanv.endreforked.api.fluids.base;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.Recipe;
import io.github.srdjanv.endreforked.api.fluids.IWorldRecipe;

public class CollisionRecipe<IN, OUT> extends Recipe<IN, OUT> implements IWorldRecipe {

    protected final ResourceLocation registryName;
    protected final int chance;
    protected final boolean consumeSource;
    protected final int consumeChance;
    protected final EventPriority priority;
    protected final BiConsumer<WorldServer, BlockPos> interactionCallback;
    protected final BiConsumer<WorldServer, BlockPos> fluidInteractionCallback;

    CollisionRecipe(IN input,
                    Function<IN, OUT> recipeFunction,
                    ResourceLocation registryName,
                    int chance,
                    boolean consumeSource,
                    int consumeChance,
                    EventPriority priority,
                    BiConsumer<WorldServer, BlockPos> interactionCallback,
                    BiConsumer<WorldServer, BlockPos> fluidInteractionCallback) {
        super(input, recipeFunction);
        this.registryName = registryName;
        this.chance = chance;
        this.consumeSource = consumeSource;
        this.consumeChance = consumeChance;
        this.priority = priority;
        this.interactionCallback = interactionCallback;
        this.fluidInteractionCallback = fluidInteractionCallback;
    }

    @Override
    public EventPriority priority() {
        return priority;
    }

    @Override
    public ResourceLocation registryName() {
        return registryName;
    }

    @Override
    public int getChance() {
        return chance;
    }

    @Override
    public int getChanceConsumeSource() {
        return consumeChance;
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

    public static <IN, OUT> Builder<IN, OUT> builder() {
        return new Builder<>();
    }

    public static final class Builder<IN, OUT> {

        private ResourceLocation registryName;
        private int chance;
        private boolean consumeSource;
        private int consumeChance;
        private EventPriority priority = EventPriority.NORMAL;
        private BiConsumer<WorldServer, BlockPos> interactionCallback = EMPTY_INTERACTION_CALLBACK;
        private BiConsumer<WorldServer, BlockPos> fluidInteractionCallback = EMPTY_FLUID_INTERACTION_CALLBACK;
        private IN input;
        private Function<IN, OUT> recipeFunction;

        private Builder() {}

        public Builder<IN, OUT> withRegistryName(ResourceLocation location) {
            this.registryName = location;
            return this;
        }

        public Builder<IN, OUT> withRegistryName(String location) {
            this.registryName = new ResourceLocation(location);
            return this;
        }

        public Builder<IN, OUT> withChance(int chance) {
            this.chance = chance;
            return this;
        }

        public Builder<IN, OUT> withConsumeChance(int consumeChance) {
            this.consumeChance = consumeChance;
            return this;
        }

        public Builder<IN, OUT> withConsumeSource(boolean consumeSource) {
            this.consumeSource = consumeSource;
            return this;
        }

        public Builder<IN, OUT> withPriority(EventPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder<IN, OUT> withInteractionCallback(BiConsumer<WorldServer, BlockPos> interactionCallback) {
            this.interactionCallback = interactionCallback;
            return this;
        }

        public Builder<IN, OUT> withFluidInteractionCallback(BiConsumer<WorldServer, BlockPos> fluidInteractionCallback) {
            this.fluidInteractionCallback = fluidInteractionCallback;
            return this;
        }

        public Builder<IN, OUT> withInput(IN input) {
            this.input = input;
            return this;
        }

        public Builder<IN, OUT> withRecipeFunction(Function<IN, OUT> recipeFunction) {
            this.recipeFunction = recipeFunction;
            return this;
        }

        public CollisionRecipe<IN, OUT> build() {
            return new CollisionRecipe<>(
                    Objects.requireNonNull(input),
                    Objects.requireNonNull(recipeFunction),
                    Objects.requireNonNull(registryName),
                    chance,
                    consumeSource,
                    consumeChance,
                    priority,
                    Objects.requireNonNull(interactionCallback),
                    Objects.requireNonNull(fluidInteractionCallback));
        }
    }
}
