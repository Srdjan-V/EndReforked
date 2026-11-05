package io.github.srdjanv.endreforked.common.configs.base;

import java.lang.reflect.Type;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public abstract class StaticServerSideConfig<D> extends BaseServerSideConfig<D> {

    public StaticServerSideConfig(String configName, Type serializationType) {
        super(configName, serializationType);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        sync(true);
    }

    @Override
    protected void dataChanged(D newData, boolean fistLoad) {
        dataLoaded(newData);
    }

    protected abstract void dataLoaded(D data);
}
