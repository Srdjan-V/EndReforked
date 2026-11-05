package io.github.srdjanv.endreforked.api.fluids;

import java.util.function.BiConsumer;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface IWorldRecipe extends Comparable<IWorldRecipe> {

    BiConsumer<WorldServer, BlockPos> EMPTY_INTERACTION_CALLBACK = (world, pos) -> {};
    BiConsumer<WorldServer, BlockPos> EMPTY_FLUID_INTERACTION_CALLBACK = (world, pos) -> {};

    EventPriority priority();

    ResourceLocation registryName();

    int getChance();

    int getChanceConsumeSource();

    boolean isConsumeSource();

    // todo remove one callback
    BiConsumer<WorldServer, BlockPos> getFluidInteractionCallback();

    BiConsumer<WorldServer, BlockPos> getInteractionCallback();

    @Override
    default int compareTo(@NotNull IWorldRecipe o) {
        return priorityToInt(priority()) - priorityToInt(o.priority());
    }

    @ApiStatus.Internal
    default int priorityToInt(EventPriority priority) {
        return switch (priority) {
            case LOWEST -> -2;
            case LOW -> -1;
            case NORMAL -> 0;
            case HIGH -> 1;
            case HIGHEST -> 2;
        };
    }
}
