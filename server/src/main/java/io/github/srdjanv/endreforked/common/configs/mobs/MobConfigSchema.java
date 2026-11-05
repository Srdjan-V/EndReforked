package io.github.srdjanv.endreforked.common.configs.mobs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.bsideup.jabel.Desugar;

import io.github.srdjanv.endreforked.common.configs.base.ResourceLocationWrapper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class MobConfigSchema {

    private final boolean register;
    private final boolean registerSpawn;
    private final SpawnConfig fallbackSpawnConfig;
    private final List<ResourceLocationWrapper> biomeWhiteList;
    private final Map<ResourceLocationWrapper, SpawnConfig> biomeWhiteListMap;

    public MobConfigSchema(boolean register, boolean registerSpawn, SpawnConfig fallbackSpawnConfig,
                           List<ResourceLocationWrapper> biomeWhiteList,
                           Map<ResourceLocationWrapper, SpawnConfig> biomeWhiteListMap) {
        this.register = register;
        this.registerSpawn = registerSpawn;
        this.fallbackSpawnConfig = fallbackSpawnConfig;
        this.biomeWhiteList = biomeWhiteList;
        this.biomeWhiteListMap = biomeWhiteListMap;
    }

    public MobConfigSchema() {
        register = false;
        registerSpawn = false;
        fallbackSpawnConfig = null;
        biomeWhiteList = Collections.emptyList();
        biomeWhiteListMap = Collections.emptyMap();
    }

    public boolean isRegister() {
        return register;
    }

    public boolean isRegisterSpawn() {
        return registerSpawn;
    }

    public SpawnConfig getFallbackSpawnConfig() {
        return fallbackSpawnConfig;
    }

    public List<ResourceLocationWrapper> getBiomeWhiteList() {
        return biomeWhiteList;
    }

    public Map<ResourceLocationWrapper, SpawnConfig> getBiomeWhiteListMap() {
        return biomeWhiteListMap;
    }

    @Desugar
    public record SpawnConfig(int spawnProbability, int maximumSpawnGroup, int minimumSpawnGroup) {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Boolean register;
        private Boolean registerSpawn;
        private SpawnConfig fallbackSpawnConfig;
        private final List<ResourceLocationWrapper> biomeSpawnList = new ObjectArrayList<>();
        private final Map<ResourceLocationWrapper, SpawnConfig> biomeSpawnListMap = new Object2ObjectOpenHashMap<>();

        private Builder() {}

        public Builder register(boolean register) {
            this.register = register;
            return this;
        }

        public Builder registerSpawn(boolean registerSpawn) {
            this.registerSpawn = registerSpawn;
            return this;
        }

        public Builder fallbackSpawnConfig(int spawnProbability, int maximumSpawnGroup, int minimumSpawnGroup) {
            return fallbackSpawnConfig(new SpawnConfig(spawnProbability, maximumSpawnGroup, minimumSpawnGroup));
        }

        public Builder fallbackSpawnConfig(SpawnConfig fallbackSpawnConfig) {
            this.fallbackSpawnConfig = fallbackSpawnConfig;
            return this;
        }

        public Builder biomeSpawn(String biome) {
            return biomeSpawn(ResourceLocationWrapper.of(biome));
        }

        public Builder biomeSpawn(ResourceLocationWrapper wrapper) {
            this.biomeSpawnList.add(wrapper);
            return this;
        }

        public Builder biomeSpawn(String biome, int spawnProbability, int maximumSpawnGroup, int minimumSpawnGroup) {
            return biomeSpawn(ResourceLocationWrapper.of(biome),
                    new SpawnConfig(spawnProbability, maximumSpawnGroup, minimumSpawnGroup));
        }

        public Builder biomeSpawn(String biome, SpawnConfig spawnConfig) {
            return biomeSpawn(ResourceLocationWrapper.of(biome), spawnConfig);
        }

        public Builder biomeSpawn(ResourceLocationWrapper wrapper, int spawnProbability, int maximumSpawnGroup,
                                  int minimumSpawnGroup) {
            return biomeSpawn(wrapper, new SpawnConfig(spawnProbability, maximumSpawnGroup, minimumSpawnGroup));
        }

        public Builder biomeSpawn(ResourceLocationWrapper wrapper, SpawnConfig spawnConfig) {
            this.biomeSpawnListMap.put(wrapper, spawnConfig);
            return this;
        }

        public MobConfigSchema build() {
            return new MobConfigSchema(Objects.requireNonNull(register),
                    Objects.requireNonNull(registerSpawn),
                    fallbackSpawnConfig, biomeSpawnList, biomeSpawnListMap);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MobConfigSchema that = (MobConfigSchema) o;
        return register == that.register && registerSpawn == that.registerSpawn &&
                Objects.equals(biomeWhiteListMap, that.biomeWhiteListMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(register, registerSpawn, biomeWhiteListMap);
    }
}
