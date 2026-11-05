package io.github.srdjanv.endreforked.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import org.jetbrains.annotations.Nullable;

import zone.rong.mixinbooter.IEarlyMixinLoader;

public class EndReForgedPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList("mixins.endreborn.json", "mixins.endreborn_dev.json");
    }

    @Override
    public boolean shouldMixinConfigQueue(String mixinConfig) {
        return switch (mixinConfig) {
            case "mixins.endreborn_dev.json" -> FMLLaunchHandler.isDeobfuscatedEnvironment();
            default -> true;
        };
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
