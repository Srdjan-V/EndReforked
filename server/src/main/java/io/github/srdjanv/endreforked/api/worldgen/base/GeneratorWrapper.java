package io.github.srdjanv.endreforked.api.worldgen.base;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.feature.WorldGenerator;

import io.github.srdjanv.endreforked.EndReforked;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class GeneratorWrapper extends WorldGenerator {

    private final WorldGenerator feature;
    private final List<WorldGeneratorBuilder> builders = new ObjectArrayList<>();

    public GeneratorWrapper(WorldGenerator feature) {
        this.feature = feature;
    }

    public GeneratorWrapper subGen(WorldGeneratorBuilder builder) {
        this.builders.add(builder);
        return this;
    }

    public GeneratorWrapper subGen(List<WorldGeneratorBuilder> builders) {
        builders.forEach(this::subGen);
        return this;
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (!(worldIn instanceof WorldServer server)) {
            EndReforked.LOGGER.error("Unable to run world generator on ClientWorld");
            return false;
        }

        if (!feature.generate(worldIn, rand, position)) return false;
        builders.stream()
                .map(builder -> builder.build(server, rand, position))
                .filter(Objects::nonNull)
                .peek(gen -> {
                    if (gen instanceof SpacedGen spacedGen) spacedGen.setSpacedGenState(true);
                })
                .forEach(gen -> gen.generate(worldIn, rand, position));

        return true;
    }
}
