package io.github.srdjanv.endreforked.common.configs.worldgen.schema;

import java.util.*;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.api.worldgen.GenConfig;
import io.github.srdjanv.endreforked.api.worldgen.Generator;
import io.github.srdjanv.endreforked.api.worldgen.WorldGeneratorBuilder;
import io.github.srdjanv.endreforked.common.configs.base.ResourceLocationWrapper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WorldGenSchema {

    public final boolean enableGeneration;
    public final int weight;
    public final GenConfig genConfigFallback;

    public final Data<Integer> dims;
    public final Data<ResourceLocationWrapper> biomes;
    public final Data<String> biome_types;

    public static class Data<D> {

        public final List<D> blackList;
        public final List<D> whiteList;
        public final Map<D, GenConfig> whiteListMap;

        public Data(List<D> blackList, List<D> whiteList, Map<D, GenConfig> whiteListMap) {
            this.blackList = blackList;
            this.whiteList = whiteList;
            this.whiteListMap = whiteListMap;
        }

        public Data() {
            this.blackList = Collections.emptyList();
            this.whiteList = Collections.emptyList();
            this.whiteListMap = Collections.emptyMap();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Data<?> data = (Data<?>) o;
            return Objects.equals(blackList, data.blackList) && Objects.equals(whiteList, data.whiteList) &&
                    Objects.equals(whiteListMap, data.whiteListMap);
        }

        @Override
        public int hashCode() {
            return Objects.hash(blackList, whiteList, whiteListMap);
        }
    }

    private WorldGenSchema(boolean enableGeneration, int weight, GenConfig genConfigFallback,
                           Data<Integer> dims,
                           Data<ResourceLocationWrapper> biomes,
                           Data<String> biome_types) {
        this.enableGeneration = enableGeneration;
        this.weight = weight;
        this.genConfigFallback = genConfigFallback;
        this.dims = dims;
        this.biomes = biomes;
        this.biome_types = biome_types;
    }

    private WorldGenSchema() {
        this.weight = 0;
        this.enableGeneration = false;
        this.genConfigFallback = null;
        this.dims = new Data<>();
        this.biomes = new Data<>();
        biome_types = new Data<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Boolean enableGeneration;
        private Integer weight = 0;
        private GenConfig genConfigFallback;

        private final DataWrapper<Integer> dimData = new DataWrapper<>();
        private final DataWrapper<ResourceLocationWrapper> biomeData = new DataWrapper<>();
        private final DataWrapper<String> biomeTypes = new DataWrapper<>();

        public Builder enableGeneration(boolean enableGeneration) {
            this.enableGeneration = enableGeneration;
            return this;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder dimConfigFallback(GenConfig genConfigFallback) {
            this.genConfigFallback = genConfigFallback;
            return this;
        }

        ////////////////////////////////////////////////////////////////////

        public Builder whiteListDim(int... dims) {
            return whiteListDim(null, dims);
        }

        public Builder whiteListDim(@Nullable GenConfig genConfig, int... dims) {
            if (genConfig == null) {
                dimData.whiteList.addAll(new IntArrayList(dims));
            } else for (int dim : dims) dimData.whiteListMap.put(dim, genConfig);
            return this;
        }

        public Builder blackListDim(int... dims) {
            dimData.blackList.addAll(new IntArrayList(dims));
            return this;
        }

        ////////////////////////////////////////////////////////////////////

        public Builder whiteListBiome(String... biomes) {
            return whiteListBiome(null,
                    Arrays.stream(biomes)
                            .map(ResourceLocationWrapper::of)
                            .toArray(ResourceLocationWrapper[]::new));
        }

        public Builder whiteListBiome(ResourceLocationWrapper... biomes) {
            return whiteListBiome(null, biomes);
        }

        public Builder whiteListBiome(@Nullable GenConfig genConfig, String... biomes) {
            return whiteListBiome(genConfig,
                    Arrays.stream(biomes)
                            .map(ResourceLocationWrapper::of)
                            .toArray(ResourceLocationWrapper[]::new));
        }

        public Builder whiteListBiome(@Nullable GenConfig genConfig, ResourceLocationWrapper... wrapper) {
            if (genConfig == null) {
                biomeData.whiteList.addAll(new ObjectArrayList<>(wrapper));
            } else for (var wrap : wrapper) biomeData.whiteListMap.put(wrap, genConfig);
            return this;
        }

        public Builder blackListBiome(String... biomes) {
            return blackListBiome(Arrays.stream(biomes)
                    .map(ResourceLocationWrapper::of)
                    .toArray(ResourceLocationWrapper[]::new));
        }

        public Builder blackListBiome(ResourceLocationWrapper... wrapper) {
            biomeData.blackList.addAll(new ObjectArrayList<>(wrapper));
            return this;
        }

        //////////////////////////////////////////////////////////////////////////

        public Builder whiteListBiomeType(BiomeDictionary.Type... types) {
            return whiteListBiomeType(Arrays.stream(types)
                    .map(BiomeDictionary.Type::getName)
                    .toArray(String[]::new));
        }

        public Builder whiteListBiomeType(GenConfig genConfig, BiomeDictionary.Type... types) {
            return whiteListBiomeType(genConfig, Arrays.stream(types)
                    .map(BiomeDictionary.Type::getName)
                    .toArray(String[]::new));
        }

        public Builder whiteListBiomeType(String... types) {
            return whiteListBiomeType(null, types);
        }

        public Builder whiteListBiomeType(GenConfig genConfig, String... types) {
            if (genConfig == null) {
                biomeTypes.whiteList.addAll(new ObjectArrayList<>(types));
            } else for (var wrap : types) biomeTypes.whiteListMap.put(wrap, genConfig);
            return this;
        }

        public Builder blackListBiomeType(BiomeDictionary.Type... types) {
            return blackListBiomeType(Arrays.stream(types)
                    .map(BiomeDictionary.Type::getName)
                    .toArray(String[]::new));
        }

        public Builder blackListBiomeType(String... types) {
            biomeTypes.blackList.addAll(new ObjectArrayList<>(types));
            return this;
        }

        ////////////////////////////////////////////////////////////////////

        public WorldGenSchema build() {
            return new WorldGenSchema(
                    Objects.requireNonNull(enableGeneration),
                    Objects.requireNonNull(weight),
                    genConfigFallback,
                    dimData.build(),
                    biomeData.build(),
                    biomeTypes.build());
        }

        private static class DataWrapper<D> {

            private final List<D> blackList = new ObjectArrayList<>(1);
            private final List<D> whiteList = new ObjectArrayList<>(1);
            private final Map<D, GenConfig> whiteListMap = new Object2ObjectOpenHashMap<>(1);

            private Data<D> build() {
                return new Data<>(blackList, whiteList, whiteListMap);
            }
        }
    }

    public Generator parseConfig(String name, WorldGeneratorBuilder worldGeneratorBuilder) {
        Generator.Builder gen = Generator.builder();
        gen.name(name)
                .weight(weight)
                .generatorBuilder(worldGeneratorBuilder);
        parseBiomes(gen);
        parseDims(gen);
        parseBiomeTypes(gen);
        gen.defaultDimConfig(genConfigFallback);
        return gen.build();
    }

    private void parseDims(Generator.Builder gen) {
        for (Integer dim : dims.blackList)
            gen.dimBlackList(dim);

        for (Integer dim : dims.whiteList)
            gen.dimWhiteList(dim);

        for (var configEntry : dims.whiteListMap.entrySet())
            gen.dimWhiteList(configEntry.getValue(), configEntry.getKey());
    }

    private void parseBiomes(Generator.Builder gen) {
        for (ResourceLocationWrapper biome : biomes.blackList) {
            final var res = resolveBiome(biome);
            if (Objects.isNull(res)) continue;
            gen.biomeBlackList(res);
        }

        for (ResourceLocationWrapper biome : biomes.whiteList) {
            final var res = resolveBiome(biome);
            if (Objects.isNull(res)) continue;
            gen.biomeWhiteList(res);
        }

        for (Map.Entry<ResourceLocationWrapper, GenConfig> entry : biomes.whiteListMap.entrySet()) {
            final var biomeRes = resolveBiome(entry.getKey());
            if (Objects.isNull(biomeRes)) continue;
            gen.biomeWhiteList(entry.getValue(), biomeRes);
        }
    }

    private void parseBiomeTypes(Generator.Builder gen) {
        for (var type : biome_types.blackList) {
            final var res = resolveBiomeType(type);
            if (Objects.isNull(res)) continue;
            gen.dimTypeBlackList(res);
        }

        for (var type : biome_types.whiteList) {
            final var res = resolveBiomeType(type);
            if (Objects.isNull(res)) continue;
            gen.dimTypeWhiteList(res);
        }

        for (var entry : biome_types.whiteListMap.entrySet()) {
            final var biomeRes = resolveBiomeType(entry.getKey());
            if (Objects.isNull(biomeRes)) continue;
            gen.dimTypeWhiteList(entry.getValue(), biomeRes);
        }
    }

    @Nullable
    private static Biome resolveBiome(ResourceLocationWrapper biome) {
        var resBiome = Biome.REGISTRY.getObject(biome.get());
        if (Objects.isNull(resBiome)) {
            EndReforked.LOGGER.warn("Unable to find biome `{}`", biome);
            return null;
        }
        return resBiome;
    }

    @Nullable
    private static BiomeDictionary.Type resolveBiomeType(String type) {
        BiomeDictionary.Type resType = null;
        type = type.toUpperCase();
        for (BiomeDictionary.Type regType : BiomeDictionary.Type.getAll()) {
            if (!regType.getName().equals(type)) continue;
            resType = regType;
            break;
        }
        if (Objects.isNull(resType)) EndReforked.LOGGER.error("Unable to find biome type `{}`", type);
        return resType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldGenSchema that = (WorldGenSchema) o;
        return enableGeneration == that.enableGeneration && weight == that.weight &&
                Objects.equals(genConfigFallback, that.genConfigFallback) &&
                Objects.equals(dims, that.dims) && Objects.equals(biomes, that.biomes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enableGeneration, weight, genConfigFallback, dims, biomes);
    }
}
