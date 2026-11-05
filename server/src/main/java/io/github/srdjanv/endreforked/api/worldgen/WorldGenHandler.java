package io.github.srdjanv.endreforked.api.worldgen;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.jetbrains.annotations.Unmodifiable;

import io.github.srdjanv.endreforked.EndReforked;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class WorldGenHandler implements IWorldGenerator {

    private static WorldGenHandler instance;

    public static WorldGenHandler getInstance() {
        if (Objects.isNull(instance)) {
            instance = new WorldGenHandler();
            GameRegistry.registerWorldGenerator(instance, 0);
        }
        return instance;
    }

    private final Set<Generator> oreGenerators = new ObjectAVLTreeSet<>();
    private final Set<Generator> genericGenerators = new ObjectAVLTreeSet<>();
    private final Set<Generator> structureGenerators = new ObjectAVLTreeSet<>();
    private final List<Runnable> mutators = new ObjectArrayList<>();

    private void mutate() {
        if (mutators.isEmpty()) return;
        synchronized (this) {
            mutators.forEach(Runnable::run);
            mutators.clear();
        }
    }

    public void clearGenerators() {
        mutators.add(() -> {
            oreGenerators.clear();
            genericGenerators.clear();
            structureGenerators.clear();
        });
    }

    public void registerOreGenerator(Generator generator) {
        mutators.add(() -> {
            if (!oreGenerators.add(generator)) {
                EndReforked.LOGGER.warn("Ignoring GenConfig: '{}'", generator.getName());
            }
        });
    }

    public void unregisterOreGenerator(Generator generator) {
        mutators.add(() -> oreGenerators.remove(generator));
    }

    public void unregisterOreGenerator(Predicate<Generator> filter) {
        mutators.add(() -> oreGenerators.removeIf(filter));
    }

    public void registerGenericGenerator(Generator generator) {
        mutators.add(() -> {
            if (!genericGenerators.add(generator)) {
                EndReforked.LOGGER.warn("Ignoring GenConfig: '{}'", generator.getName());
            }
        });
    }

    public void unregisterGenericGenerator(Generator generator) {
        mutators.add(() -> genericGenerators.remove(generator));
    }

    public void unregisterGenericGenerator(Predicate<Generator> filter) {
        mutators.add(() -> genericGenerators.removeIf(filter));
    }

    public void registerStructureGenerator(Generator generator) {
        mutators.add(() -> {
            if (!structureGenerators.add(generator)) {
                EndReforked.LOGGER.warn("Ignoring GenConfig: '{}'", generator.getName());
            }
        });
    }

    public void unregisterStructureGenerator(Generator generator) {
        mutators.add(() -> structureGenerators.remove(generator));
    }

    public void unregisterStructureGenerator(Predicate<Generator> filter) {
        mutators.add(() -> structureGenerators.removeIf(filter));
    }

    private final Set<Generator> unmodifiableOreGenerators = Collections
            .unmodifiableSet(oreGenerators);

    @Unmodifiable
    public Set<Generator> getOreGenerators() {
        return unmodifiableOreGenerators;
    }

    private final Set<Generator> unmodifiableGenericGenerators = Collections
            .unmodifiableSet(genericGenerators);

    @Unmodifiable
    public Set<Generator> getGenericGenerators() {
        return unmodifiableGenericGenerators;
    }

    private final Set<Generator> unmodifiableStructureGenerators = Collections
            .unmodifiableSet(structureGenerators);

    @Unmodifiable
    public Set<Generator> getStructureGenerators() {
        return unmodifiableStructureGenerators;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
                         IChunkProvider chunkProvider) {
        mutate();

        final var biome = world.getBiome(new BlockPos(
                (chunkX << 4) + 8,
                world.getActualHeight(),
                (chunkZ << 4) + 8));
        getApplicableGenerators(oreGenerators, world, biome).forEach(gen -> {
            runChunkGenerator(gen, biome, world, random, chunkX, chunkZ);
        });

        getApplicableGenerators(genericGenerators, world, biome).forEach(gen -> {
            runChunkGenerator(gen, biome, world, random, chunkX, chunkZ);
        });

        getApplicableGenerators(structureGenerators, world, biome).forEach(gen -> {
            runChunkGenerator(gen, biome, world, random, chunkX, chunkZ);
        });
    }

    private void runChunkGenerator(Generator config, Biome biome, World world,
                                   Random rand, int chunkX, int chunkZ) {
        final var dimConfig = config.getDimConfig(world.provider.getDimension(), biome);
        if (Objects.isNull(dimConfig)) return;
        {
            final var rarity = dimConfig.modifier(Modifier.RARITY);
            if (rarity.isPresent() && rarity.getAsInt() >= 1 && rand.nextInt(rarity.getAsInt()) != 0) return;
        }
        final var gen = config.getGenerator(world, biome, dimConfig);
        if (Objects.isNull(gen)) return;

        try {
            if (gen instanceof WorldGenMinable) {
                int heightDiff = dimConfig.maxHeight() - dimConfig.minHeight();
                var pos = new BlockPos(
                        chunkX << 4,
                        dimConfig.minHeight() + rand.nextInt(Math.max(1, heightDiff)),
                        chunkZ << 4);
                gen.generate(world, rand, pos.add(8, 0, 8));
            } else {
                gen.generate(world, rand, new BlockPos(
                        chunkX << 4,
                        world.getSeaLevel(),
                        chunkZ << 4));

            }
        } catch (Exception e) {
            throw new WorldGeneratorException(
                    MessageFormat.format("Failed to run chunk generator: {0}", config.getName()),
                    e);
        }
    }

    public static Stream<Generator> getApplicableGenerators(
                                                            final Set<Generator> generatorMap,
                                                            final World world,
                                                            final Biome biome) {
        final var dim = world.provider.getDimension();
        return generatorMap.stream()
                .filter(entry -> entry.isValidDimension(dim))
                .filter(entry -> entry.isValidBiome(biome));
    }
}
