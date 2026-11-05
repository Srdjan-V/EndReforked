package io.github.srdjanv.endreforked.api.worldgen.base;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import io.github.srdjanv.endreforked.api.worldgen.GenConfig;
import io.github.srdjanv.endreforked.api.worldgen.Modifier;

public interface SpacedGen {

    void setSpacedGenState(boolean disabled);

    boolean isSpacedGenDisabled();

    default boolean shouldGenSpaced(GenConfig config) {
        return !isSpacedGenDisabled() && config.modifier(Modifier.UNIQUE_GENERATOR_ID).isPresent();
    }

    default boolean validateGenSpacing(WorldServer server, GenConfig config, BlockPos position) {
        return validateGenSpacing(server, config, position.getX() >> 4, position.getZ() >> 4);
    }

    default boolean validateGenSpacing(WorldServer server, GenConfig config, int chunkX, int chunkZ) {
        int orgX = chunkX;
        int orgZ = chunkZ;
        final int uniqueGeneratorId = config.uniqueGeneratorId();
        final int spacing = config.spacing();
        final int separation = config.separation();

        if (chunkX < 0) chunkX -= spacing - 1;
        if (chunkZ < 0) chunkZ -= spacing - 1;

        int computedX = chunkX / spacing;
        int computedZ = chunkZ / spacing;
        Random random = server.setRandomSeed(computedX, computedZ, uniqueGeneratorId);
        computedX = computedX * spacing;
        computedZ = computedZ * spacing;
        computedX = computedX + (random.nextInt(spacing - separation) + random.nextInt(spacing - separation)) / 2;
        computedZ = computedZ + (random.nextInt(spacing - separation) + random.nextInt(spacing - separation)) / 2;
        return orgX == computedX && orgZ == computedZ;
    }
}
