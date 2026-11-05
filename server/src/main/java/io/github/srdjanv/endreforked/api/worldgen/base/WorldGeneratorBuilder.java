package io.github.srdjanv.endreforked.api.worldgen.base;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.feature.WorldGenerator;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface WorldGeneratorBuilder {

    @Nullable
    WorldGenerator build(WorldServer server, Random rand, BlockPos position);
}
