package io.github.srdjanv.endreforked.client

import io.github.srdjanv.endreforked.client.entity.render.*
import io.github.srdjanv.endreforked.common.configs.Configs
import io.github.srdjanv.endreforked.common.entity.*
import io.github.srdjanv.endreforked.utils.Initializer
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

internal class Registration : Initializer {
    override fun registerEventBus() = registerThisToEventBus()

    @SubscribeEvent
    fun registerModels(event: ModelRegistryEvent) {
        RenderingRegistry.registerEntityRenderingHandler(EntityEntropyFireball::class.java, ::RenderColdFireball)
        RenderingRegistry.registerEntityRenderingHandler(EntityEndGuard::class.java, ::RenderEGuard)
        RenderingRegistry.registerEntityRenderingHandler(EntityWatcher::class.java, ::RenderWatcher)
        RenderingRegistry.registerEntityRenderingHandler(EntityChronologist::class.java, ::RenderChronologist)
    }

    override fun init(event: FMLInitializationEvent) {
        if (Configs.CLIENT_SIDE_CONFIGS.panorama) GuiMainMenuEnd.endMainMenu()
    }
}
