package io.github.srdjanv.endreforked.api.worldgen.base;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.worldgen.GenConfig;
import io.github.srdjanv.endreforked.api.worldgen.Generator;

// Unfinished
public class SimpleMapGenStructure extends MapGenStructure {

    private final Generator generator;
    private final StructureFunction function;

    public SimpleMapGenStructure(Generator generator, StructureFunction function) {
        this.generator = generator;
        this.function = function;
    }

    @Override
    public String getStructureName() {
        return generator.getName();
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
        this.world = worldIn;
        return null;
    }

    @Override
    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        var biome = world.getBiome(new BlockPos(chunkX >> 4, 0, chunkZ >> 4));
        var config = generator.getDimConfig(world.provider.getDimension(), biome);
        return config != null;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        var biome = world.getBiome(new BlockPos(chunkX >> 4, 0, chunkZ >> 4));
        var config = generator.getDimConfig(world.provider.getDimension(), biome);
        return function.apply((WorldServer) world, config, rand, chunkX, chunkZ);
    }

    public interface StructureFunction {

        StructureStart apply(WorldServer server, GenConfig genConfig, Random random, int chunkX, int chunkZ);
    }
}
