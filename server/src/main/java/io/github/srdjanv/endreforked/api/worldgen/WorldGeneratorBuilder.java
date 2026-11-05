package io.github.srdjanv.endreforked.api.worldgen;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface WorldGeneratorBuilder {

    @Nullable
    WorldGenerator build(@NotNull World world, @Nullable Biome biome, @NotNull GenConfig config);
}
