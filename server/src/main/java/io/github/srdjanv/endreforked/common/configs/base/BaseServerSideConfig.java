package io.github.srdjanv.endreforked.common.configs.base;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Objects;

import net.minecraftforge.fml.relauncher.FMLInjectionData;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.configs.JsonConfigs;
import io.github.srdjanv.endreforked.utils.Initializer;

public abstract class BaseServerSideConfig<D> implements Initializer {

    private final File configPath;
    private final String configName;
    private final Type serializationType;

    public BaseServerSideConfig(String configName, Type serializationType) {
        this.configName = configName;
        this.configPath = new File(((File) FMLInjectionData.data()[6]).getAbsolutePath(),
                "config/" + Tags.MODID + "/" + configName + ".json");
        this.serializationType = serializationType;
        JsonConfigs.addConfig(this);
    }

    public File getConfigPath() {
        return configPath;
    }

    public String getConfigName() {
        return configName;
    }

    public void loadDefaults() {
        EndReforked.LOGGER.info("Loading default data for config '{}'", configName);
        dataChanged(getDefaultData(), false);
    }

    public void saveDefaults() {
        EndReforked.LOGGER.info("Saving default data for config '{}'", configName);
        writeData(getDefaultData());
    }

    protected void sync(boolean fistLoad) {
        D loadedData;
        try {
            loadedData = readData();
        } catch (JsonSyntaxException e) {
            EndReforked.LOGGER.error("Error loading json config: {}, loading defaults." +
                    " Fix the typo and reload the config", configName);
            EndReforked.LOGGER.error(e);
            loadedData = getDefaultData();
            dataChanged(loadedData, fistLoad);
            return;
        } catch (JsonIOException e) {
            EndReforked.LOGGER.error("Error loading json config: {}, loading defaults.", configName);
            EndReforked.LOGGER.error(e);
            loadedData = getDefaultData();
            dataChanged(loadedData, fistLoad);
            return;
        } catch (FileNotFoundException e) {
            loadedData = getDefaultData();
            writeData(loadedData);
            dataChanged(loadedData, fistLoad);
            return;
        }

        if (!validateLoadedData(loadedData) || !Objects.equals(getLoadedData(), loadedData)) {
            D mergedData = dataMerger(getDefaultData(), loadedData, getLoadedData(), fistLoad);
            if (!Objects.equals(mergedData, loadedData)) writeData(mergedData);
            dataChanged(mergedData, fistLoad);
        }
    }

    protected D dataMerger(D defaultData, D loadedData, D existingData, boolean fistLoad) {
        return existingData;
    }

    protected boolean validateLoadedData(D newLoadedData) {
        return true;
    }

    protected void dataChanged(D newData, boolean fistLoad) {}

    protected abstract D getDefaultData();

    @Nullable
    protected abstract D getLoadedData();

    protected D readData() throws FileNotFoundException, JsonIOException, JsonSyntaxException {
        return getReader().create().fromJson(new JsonReader(new FileReader(configPath)), serializationType);
    }

    protected void writeData(D data) {
        try {
            var jsonData = getWriter().create().toJson(data);
            FileUtils.write(configPath, jsonData, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected GsonBuilder getWriter() {
        return new GsonBuilder().setPrettyPrinting()
                .enableComplexMapKeySerialization();
    }

    protected GsonBuilder getReader() {
        return new GsonBuilder();
    }
}
