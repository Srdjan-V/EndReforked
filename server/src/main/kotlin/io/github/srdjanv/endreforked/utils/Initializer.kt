package io.github.srdjanv.endreforked.utils

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.*

interface Initializer {
    fun registerEventBus() {}

    fun registerThisToEventBus() = MinecraftForge.EVENT_BUS.register(this)

    fun preInit(event: FMLPreInitializationEvent) {}
    fun init(event: FMLInitializationEvent) {}
    fun postInit(event: FMLPostInitializationEvent) {}
    fun onServerStarted(event: FMLServerStartedEvent) {}
    fun onServerStopped(event: FMLServerStoppedEvent) {}

    fun dispose(): Boolean = false
    fun onDispose() {}
}
