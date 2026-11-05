package io.github.srdjanv.endreforked.common.configs.worldgen.base;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.reflect.TypeToken;

import io.github.srdjanv.endreforked.api.worldgen.WorldGeneratorBuilder;
import io.github.srdjanv.endreforked.common.configs.base.ReloadableServerSideConfig;
import io.github.srdjanv.endreforked.common.configs.worldgen.schema.WorldGenSchema;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public abstract class WorldGenBaseConfigReloadable extends ReloadableServerSideConfig<Map<String, WorldGenSchema>> {

    protected final Map<String, Function<WorldGenSchema.Builder, WorldGenSchema>> defaultData = new Object2ObjectOpenHashMap<>();
    protected final Map<String, WorldGeneratorBuilder> nameToGenerator = new Object2ObjectOpenHashMap<>();
    protected Map<String, WorldGenSchema> loadedDataData;

    protected WorldGenBaseConfigReloadable(String configName) {
        super(configName, new TypeToken<Map<String, WorldGenSchema>>() {}.getType());
    }

    protected void registerGen(String name,
                               Function<WorldGenSchema.Builder, WorldGenSchema> worldGenConfig,
                               WorldGeneratorBuilder builder) {
        defaultData.put(name, worldGenConfig);
        nameToGenerator.put(name, builder);
    }

    @Override
    protected Map<String, WorldGenSchema> getDefaultData() {
        var atomic = new AtomicInteger(10000);
        return defaultData.entrySet().stream().map(
                stringFunctionEntry -> Pair.of(stringFunctionEntry.getKey(),
                        stringFunctionEntry.getValue().apply(
                                WorldGenSchema.builder()
                                        .enableGeneration(true)
                                        .weight(atomic.getAndAdd(200)))))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Override
    protected Map<String, WorldGenSchema> getLoadedData() {
        return loadedDataData;
    }

    @Override
    protected boolean validateLoadedData(Map<String, WorldGenSchema> newLoadedData) {
        return newLoadedData.keySet().containsAll(defaultData.keySet());
    }

    @Override
    protected Map<String, WorldGenSchema> dataMerger(Map<String, WorldGenSchema> defaultData,
                                                     Map<String, WorldGenSchema> loadedData,
                                                     Map<String, WorldGenSchema> existingData, boolean fistLoad) {
        if (fistLoad) {
            Map<String, WorldGenSchema> data = new Object2ObjectOpenHashMap<>();
            for (var entry : defaultData.entrySet()) {
                WorldGenSchema worldGenSchema = loadedData.get(entry.getKey());
                if (Objects.isNull(worldGenSchema)) worldGenSchema = entry.getValue();
                data.put(entry.getKey(), worldGenSchema);
            }

            return data;
        }

        Map<String, WorldGenSchema> data = new Object2ObjectOpenHashMap<>();
        for (var entry : defaultData.entrySet()) {
            WorldGenSchema worldGenSchema = loadedData.get(entry.getKey());
            if (Objects.isNull(worldGenSchema)) worldGenSchema = existingData.get(entry.getKey());
            if (Objects.isNull(worldGenSchema)) worldGenSchema = entry.getValue();
            data.put(entry.getKey(), worldGenSchema);
        }
        return data;
    }

    @Override
    protected void dataChanged(Map<String, WorldGenSchema> newData, boolean fistLoad) {
        loadedDataData = newData;
        if (fistLoad) {
            registerToHandler();
        } else {
            unRegisterFromHandler();
            registerToHandler();
        }
    }

    protected abstract void registerToHandler();

    protected abstract void unRegisterFromHandler();
}
