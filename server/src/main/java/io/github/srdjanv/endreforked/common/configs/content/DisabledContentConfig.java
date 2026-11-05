package io.github.srdjanv.endreforked.common.configs.content;

import java.util.Objects;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.common.configs.base.StaticServerSideConfig;

public class DisabledContentConfig extends StaticServerSideConfig<DisabledContentSchema> {

    private static DisabledContentConfig instance;

    public static DisabledContentConfig getInstance() {
        if (Objects.isNull(instance)) {
            instance = new DisabledContentConfig();
            EndReforked.LOGGER.debug("Instantiating {}", instance);
        }
        return instance;
    }

    @Override
    public void init(FMLInitializationEvent event) {}

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        sync(true);
    }

    private DisabledContentSchema loadedData;

    private DisabledContentConfig() {
        super("disabled_content", DisabledContentSchema.class);
    }

    @Override
    protected DisabledContentSchema getDefaultData() {
        return new DisabledContentSchema();
    }

    @Override
    @Nullable
    public DisabledContentSchema getLoadedData() {
        return loadedData;
    }

    @Override
    protected void dataLoaded(DisabledContentSchema data) {
        loadedData = data;
    }
}
