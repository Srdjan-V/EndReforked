package io.github.srdjanv.endreforked.api.worldgen.base;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

@FunctionalInterface
public interface PositionGenerator {

    boolean generate(WorldServer server, Random rand, BlockPos pos);
}
