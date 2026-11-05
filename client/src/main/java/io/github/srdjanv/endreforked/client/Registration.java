package io.github.srdjanv.endreforked.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import io.github.srdjanv.endreforked.client.entity.render.*;
import io.github.srdjanv.endreforked.common.configs.Configs;
import io.github.srdjanv.endreforked.common.entity.*;
import io.github.srdjanv.endreforked.utils.Initializer;

final class Registration implements Initializer {

    @Override
    public void registerEventBus() {
        registerThisToEventBus();
    }

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityEndGuard.class, RenderEGuard.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityWatcher.class, RenderWatcher.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityLord.class, RenderLord.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityChronologist.class, RenderChronologist.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityEntropyFireball.class, RenderColdFireball::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityEndGuard.class, RenderEGuard::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityWatcher.class, RenderWatcher::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityLord.class, RenderLord::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityChronologist.class, RenderChronologist::new);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        if (Configs.CLIENT_SIDE_CONFIGS.panorama) {
            GuiMainMenuEnd.endMainMenu();
        }
    }
}
