package io.github.srdjanv.endreforked.compat;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.*;

import io.github.srdjanv.endreforked.compat.baubles.BaublesCompat;
import io.github.srdjanv.endreforked.compat.tconstruct.TConstruct;
import io.github.srdjanv.endreforked.utils.Initializer;

public class CompatManger implements Initializer {

    private static CompatManger instance;

    public static CompatManger getInstance() {
        if (instance == null) instance = new CompatManger();
        return instance;
    }

    public static void invalidate() {
        instance = null;
    }

    private final List<ModCompat> mods;

    private CompatManger() {
        this.mods = Stream.of(new TConstruct(), new BaublesCompat())
                .filter(mod -> Loader.isModLoaded(mod.modID())).collect(Collectors.toList());
    }

    @Override
    public void registerEventBus() {
        mods.forEach(ModCompat::registerEventBus);
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        mods.forEach(mod -> mod.preInit(event));
    }

    @Override
    public void init(FMLInitializationEvent event) {
        mods.forEach(mod -> mod.init(event));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        mods.forEach(mod -> mod.postInit(event));
    }

    @Override
    public void onServerStarted(FMLServerStartedEvent event) {
        mods.forEach(mod -> mod.onServerStarted(event));
    }

    @Override
    public void onServerStopped(FMLServerStoppedEvent event) {
        mods.forEach(mod -> mod.onServerStopped(event));
    }

    @Override
    public boolean dispose() {
        return true;
    }

    @Override
    public void onDispose() {
        mods.forEach(Initializer::onDispose);
    }

    public interface ModCompat extends Initializer {

        String modID();
    }
}
