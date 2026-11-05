package io.github.srdjanv.endreforked.common.comands.dev;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import io.github.srdjanv.endreforked.Tags;

public class DevConfigs {

    public final static Configuration CONFIGURATION = new Configuration(new File("config/" + Tags.MODID + "/dev.cfg"));

    // public final static Property DISABLE_REGION_LOADING = CONFIGURATION.get("dev", "disable_region_loading", false);

    static {
        sync();
    }

    public static void sync() {
        if (CONFIGURATION.hasChanged()) CONFIGURATION.save();
    }
}
