package io.github.srdjanv.endreforked.common.configs.base;

import java.lang.reflect.Type;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;

public abstract class ReloadableServerSideConfig<D> extends BaseServerSideConfig<D> {

    public ReloadableServerSideConfig(String configName, Type serializationType) {
        super(configName, serializationType);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        sync(true);
    }

    public void sync() {
        sync(false);
    }
}
