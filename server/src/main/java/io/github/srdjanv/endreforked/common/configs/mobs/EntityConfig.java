package io.github.srdjanv.endreforked.common.configs.mobs;

import java.util.Objects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.Tags;

public class EntityConfig {

    private final String name;
    private final Class<? extends Entity> entityClass;
    private final EnumCreatureType creatureType;
    private final boolean registerEgg;
    private final int id;
    private final int trackingRange, updateFrequency;
    private final boolean sendsVelocityUpdates;
    private final int eggPrimary, eggSecondary;

    public EntityConfig(String name, Class<? extends Entity> entityClass, EnumCreatureType creatureType,
                        int id, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int eggPrimary,
                        int eggSecondary) {
        this.name = name;
        this.entityClass = entityClass;
        this.creatureType = creatureType;
        this.id = id;
        this.trackingRange = trackingRange;
        this.updateFrequency = updateFrequency;
        this.sendsVelocityUpdates = sendsVelocityUpdates;
        this.eggPrimary = eggPrimary;
        this.eggSecondary = eggSecondary;
        registerEgg = true;
    }

    public EntityConfig(String name, Class<? extends Entity> entityClass, EnumCreatureType creatureType,
                        int id, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
        this.name = name;
        this.entityClass = entityClass;
        this.creatureType = creatureType;
        this.id = id;
        this.trackingRange = trackingRange;
        this.updateFrequency = updateFrequency;
        this.sendsVelocityUpdates = sendsVelocityUpdates;
        this.eggPrimary = 0;
        this.eggSecondary = 0;
        registerEgg = false;
    }

    public void init(MobConfigSchema configSchema) {
        if (!configSchema.isRegister()) return;
        registerEntity(name);
        if (configSchema.isRegisterSpawn()) {
            registerSpawn(configSchema);
        }
    }

    private void registerEntity(String name) {
        if (registerEgg) {
            EntityRegistry.registerModEntity(new ResourceLocation(Tags.MODID, name), entityClass, name,
                    id, EndReforked.instance, trackingRange, updateFrequency, sendsVelocityUpdates,
                    eggPrimary, eggSecondary);
        } else {
            EntityRegistry.registerModEntity(new ResourceLocation(Tags.MODID, name), entityClass, name,
                    id, EndReforked.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
        }
    }

    private void registerSpawn(MobConfigSchema configSchema) {
        configSchema.getBiomeWhiteListMap().forEach((wrapper, spawnConfig) -> {
            var biome = Biome.REGISTRY.getObject(wrapper.get());
            if (Objects.isNull(biome)) throw new IllegalArgumentException(wrapper.get().toString());
            Class<? extends EntityLiving> entityLiving = entityClass.asSubclass(EntityLiving.class);
            EntityRegistry.addSpawn(entityLiving,
                    spawnConfig.spawnProbability(),
                    spawnConfig.minimumSpawnGroup(),
                    spawnConfig.maximumSpawnGroup(),
                    creatureType,
                    biome);
        });

        if (Objects.isNull(configSchema.getFallbackSpawnConfig()) && !configSchema.getBiomeWhiteList().isEmpty()) {
            throw new RuntimeException(); // TODO
        }
        configSchema.getBiomeWhiteList().forEach(wrapper -> {
            var biome = Biome.REGISTRY.getObject(wrapper.get());
            if (Objects.isNull(biome)) throw new IllegalArgumentException(wrapper.get().toString());
            Class<? extends EntityLiving> entityLiving = entityClass.asSubclass(EntityLiving.class);
            EntityRegistry.addSpawn(entityLiving,
                    configSchema.getFallbackSpawnConfig().spawnProbability(),
                    configSchema.getFallbackSpawnConfig().minimumSpawnGroup(),
                    configSchema.getFallbackSpawnConfig().maximumSpawnGroup(),
                    creatureType,
                    biome);
        });
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String name;
        private Class<? extends Entity> entityClass;
        private EnumCreatureType creatureType;
        private Boolean registerEgg;
        private Integer id;
        private Integer trackingRange;
        private Integer updateFrequency;
        private Boolean sendsVelocityUpdates;
        private Integer eggPrimary;
        private Integer eggSecondary;

        private Builder() {}

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder entityClass(Class<? extends Entity> entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public Builder creatureType(EnumCreatureType creatureType) {
            this.creatureType = creatureType;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder registerEgg(boolean registerEgg) {
            this.registerEgg = registerEgg;
            return this;
        }

        public Builder trackingRange(int trackingRange) {
            this.trackingRange = trackingRange;
            return this;
        }

        public Builder updateFrequency(int updateFrequency) {
            this.updateFrequency = updateFrequency;
            return this;
        }

        public Builder sendsVelocityUpdates(boolean sendsVelocityUpdates) {
            this.sendsVelocityUpdates = sendsVelocityUpdates;
            return this;
        }

        public Builder eggPrimary(int eggPrimary) {
            this.eggPrimary = eggPrimary;
            return this;
        }

        public Builder eggSecondary(int eggSecondary) {
            this.eggSecondary = eggSecondary;
            return this;
        }

        public EntityConfig build() {
            if (registerEgg) {
                return new EntityConfig(
                        Objects.requireNonNull(name),
                        Objects.requireNonNull(entityClass),
                        creatureType,
                        Objects.requireNonNull(id),
                        Objects.requireNonNull(trackingRange),
                        Objects.requireNonNull(updateFrequency),
                        Objects.requireNonNull(sendsVelocityUpdates),
                        Objects.requireNonNull(eggPrimary), Objects.requireNonNull(eggSecondary));
            } else {
                return new EntityConfig(
                        Objects.requireNonNull(name),
                        Objects.requireNonNull(entityClass),
                        creatureType,
                        Objects.requireNonNull(id),
                        Objects.requireNonNull(trackingRange),
                        Objects.requireNonNull(updateFrequency),
                        Objects.requireNonNull(sendsVelocityUpdates));
            }
        }
    }
}
