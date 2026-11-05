package io.github.srdjanv.endreforked.api.fluids.base;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.BaseRecipe;
import io.github.srdjanv.endreforked.api.fluids.IWorldRecipe;

public class EntityFluidRecipe<EntityIn extends Entity, EntityOut extends Entity>
                              extends
                              BaseRecipe<Class<EntityIn>, BiFunction<World, EntityIn, EntityFluidRecipeResult<EntityOut>>>
                              implements IWorldRecipe {

    protected final ResourceLocation registryName;
    protected final int chance;
    protected final boolean consumeSource;
    protected final int consumeChance;
    protected final boolean removeEntity;
    protected final EventPriority priority;
    protected final EntityMather<EntityIn> entityMather;
    protected final BiConsumer<WorldServer, BlockPos> interactionCallback;
    protected final BiConsumer<WorldServer, BlockPos> fluidInteractionCallback;

    EntityFluidRecipe(Class<EntityIn> input,
                      BiFunction<World, EntityIn, EntityFluidRecipeResult<EntityOut>> entityFunction,
                      ResourceLocation registryName,
                      int chance,
                      boolean consumeSource,
                      int consumeChance,
                      boolean removeEntity,
                      EventPriority priority,
                      EntityMather<EntityIn> entityMather,
                      BiConsumer<WorldServer, BlockPos> interactionCallback,
                      BiConsumer<WorldServer, BlockPos> fluidInteractionCallback) {
        super(input, entityFunction);
        this.registryName = registryName;
        this.chance = chance;
        this.consumeSource = consumeSource;
        this.consumeChance = consumeChance;
        this.removeEntity = removeEntity;
        this.priority = priority;
        this.entityMather = entityMather;
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

    public EntityMather<EntityIn> getEntityMather() {
        return entityMather;
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

    public boolean isRemoveEntity() {
        return removeEntity;
    }

    @Override
    public BiConsumer<WorldServer, BlockPos> getFluidInteractionCallback() {
        return fluidInteractionCallback;
    }

    @Override
    public BiConsumer<WorldServer, BlockPos> getInteractionCallback() {
        return interactionCallback;
    }

    public static <EntityIn extends Entity, EntityOut extends Entity> Builder<EntityIn, EntityOut> builder() {
        return new Builder<>();
    }

    public static final class Builder<EntityIn extends Entity, EntityOut extends Entity> {

        private ResourceLocation registryName;
        private int chance;
        private boolean consumeSource;
        private int consumeChance;
        private boolean removeEntity;
        private EventPriority priority = EventPriority.NORMAL;
        private EntityMather<EntityIn> entityMather;
        private BiConsumer<WorldServer, BlockPos> interactionCallback = CollisionRecipe.EMPTY_INTERACTION_CALLBACK;
        private BiConsumer<WorldServer, BlockPos> fluidInteractionCallback = CollisionRecipe.EMPTY_FLUID_INTERACTION_CALLBACK;
        private Class<EntityIn> clazz;
        private BiFunction<World, EntityIn, EntityFluidRecipeResult<EntityOut>> entityFunction;

        private Builder() {}

        public Builder<EntityIn, EntityOut> withRegistryName(ResourceLocation location) {
            this.registryName = location;
            return this;
        }

        public Builder<EntityIn, EntityOut> withRegistryName(String location) {
            this.registryName = new ResourceLocation(location);
            return this;
        }

        public Builder<EntityIn, EntityOut> withChance(int chance) {
            this.chance = chance;
            return this;
        }

        public Builder<EntityIn, EntityOut> withConsumeSource(boolean consumeSource) {
            this.consumeSource = consumeSource;
            return this;
        }

        public Builder<EntityIn, EntityOut> withConsumeChance(int consumeChance) {
            this.consumeChance = consumeChance;
            return this;
        }

        public Builder<EntityIn, EntityOut> withRemoveEntity(boolean removeEntity) {
            this.removeEntity = removeEntity;
            return this;
        }

        public Builder<EntityIn, EntityOut> withPriority(EventPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder<EntityIn, EntityOut> withEntityMather(EntityMather<EntityIn> entityMather) {
            this.entityMather = entityMather;
            return this;
        }

        public Builder<EntityIn, EntityOut> withInteractionCallback(BiConsumer<WorldServer, BlockPos> interactionCallback) {
            this.interactionCallback = interactionCallback;
            return this;
        }

        public Builder<EntityIn, EntityOut> withFluidInteractionCallback(BiConsumer<WorldServer, BlockPos> fluidInteractionCallback) {
            this.fluidInteractionCallback = fluidInteractionCallback;
            return this;
        }

        public Builder<EntityIn, EntityOut> withClazz(Class<EntityIn> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder<EntityIn, EntityOut> withEntityFunction(BiFunction<World, EntityIn, EntityFluidRecipeResult<EntityOut>> entityFunction) {
            this.entityFunction = entityFunction;
            return this;
        }

        public EntityFluidRecipe<EntityIn, EntityOut> build() {
            return new EntityFluidRecipe<>(
                    Objects.requireNonNull(clazz),
                    Objects.requireNonNull(entityFunction),
                    Objects.requireNonNull(registryName),
                    chance,
                    consumeSource,
                    consumeChance,
                    removeEntity,
                    priority,
                    Objects.requireNonNull(entityMather),
                    Objects.requireNonNull(interactionCallback), Objects.requireNonNull(fluidInteractionCallback));
        }
    }
}
