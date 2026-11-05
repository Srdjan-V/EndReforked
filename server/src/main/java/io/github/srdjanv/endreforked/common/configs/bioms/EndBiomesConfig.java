package io.github.srdjanv.endreforked.common.configs.bioms;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.google.gson.reflect.TypeToken;

import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent;
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry;
import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.common.ModBioms;
import io.github.srdjanv.endreforked.common.configs.base.ReloadableServerSideConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class EndBiomesConfig extends ReloadableServerSideConfig<Map<String, BiomeSchema>> {

    private static EndBiomesConfig instance;

    public static EndBiomesConfig getInstance() {
        if (Objects.isNull(instance)) {
            instance = new EndBiomesConfig();
            EndReforked.LOGGER.debug("Instantiating {}", instance);
        }
        return instance;
    }

    private final Map<String, Function<BiomeSchema.Builder, BiomeSchema>> defaultData = new Object2ObjectOpenHashMap<>();
    private final Map<String, Supplier<? extends Biome>> name2biome = new Object2ObjectOpenHashMap<>();
    private Map<String, BiomeSchema> loadedDataData;

    private EndBiomesConfig() {
        super("end_biomes", new TypeToken<Map<String, BiomeSchema>>() {}.getType());
    }

    @Override
    public void registerEventBus() {
        registerThisToEventBus();
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        registerBiome("organa",
                ModBioms.ORGANA_BIOME,
                builder -> builder.weight(1000).build());

        registerBiome("entropy",
                ModBioms.ENTROPY_BIOME,
                builder -> builder.weight(1000).build());
    }

    private void registerBiome(String name,
                               Supplier<? extends Biome> biome,
                               Function<BiomeSchema.Builder, BiomeSchema> function) {
        name2biome.put(name, biome);
        defaultData.put(name, function);
    }

    @Override
    protected Map<String, BiomeSchema> getDefaultData() {
        return defaultData.entrySet()
                .stream()
                .map(entry -> Pair.of(entry.getKey(),
                        entry.getValue().apply(BiomeSchema.builder().enabled(true))))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Override
    protected @Nullable Map<String, BiomeSchema> getLoadedData() {
        return loadedDataData;
    }

    @Override
    protected void dataChanged(Map<String, BiomeSchema> newData, boolean fistLoad) {
        loadedDataData = newData;
        if (fistLoad) {
            register();
        } else {
            unRegister();
            register();
        }
    }

    @Override
    protected Map<String, BiomeSchema> dataMerger(
                                                  Map<String, BiomeSchema> defaultData,
                                                  Map<String, BiomeSchema> loadedData,
                                                  Map<String, BiomeSchema> existingData,
                                                  boolean fistLoad) {
        if (fistLoad) {
            Map<String, BiomeSchema> data = new Object2ObjectOpenHashMap<>();
            for (var entry : defaultData.entrySet()) {
                BiomeSchema biomeSchema = loadedData.get(entry.getKey());
                if (Objects.isNull(biomeSchema)) biomeSchema = entry.getValue();
                data.put(entry.getKey(), biomeSchema);
            }

            return data;
        }

        Map<String, BiomeSchema> data = new Object2ObjectOpenHashMap<>();
        for (var entry : defaultData.entrySet()) {
            BiomeSchema biomeSchema = loadedData.get(entry.getKey());
            if (Objects.isNull(biomeSchema)) biomeSchema = existingData.get(entry.getKey());
            if (Objects.isNull(biomeSchema)) biomeSchema = entry.getValue();
            data.put(entry.getKey(), biomeSchema);
        }
        return data;
    }

    @SubscribeEvent
    public void registerNetherAPI(@Nonnull NetherAPIRegistryEvent.End event) {
        if (loadedDataData == null) return;
        loadedDataData.forEach((name, biomeSchema) -> {
            if (!biomeSchema.isEnabled()) return;
            var biome = name2biome.get(name);
            NetherAPIRegistry.THE_END.registerBiome(biome.get(), biomeSchema.getWeight());
        });
    }

    private void register() {
        loadedDataData.forEach((name, biomeSchema) -> {
            if (!biomeSchema.isEnabled()) return;
            var biome = name2biome.get(name);
            NetherAPIRegistry.THE_END.registerBiome(biome.get(), biomeSchema.getWeight());
        });
    }

    private void unRegister() {
        loadedDataData.forEach((name, biomeSchema) -> {
            var biome = name2biome.get(name);
            NetherAPIRegistry.THE_END.registerBiome(biome.get(), biomeSchema.getWeight());
        });
    }
}
