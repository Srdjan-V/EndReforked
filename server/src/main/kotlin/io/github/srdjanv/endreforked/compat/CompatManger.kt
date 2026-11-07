package io.github.srdjanv.endreforked.compat

import io.github.srdjanv.endreforked.compat.baubles.BaublesCompat
import io.github.srdjanv.endreforked.compat.conarm.ConArm
import io.github.srdjanv.endreforked.compat.tconstruct.TConstruct
import io.github.srdjanv.endreforked.compat.theoneprobe.TheOneProbeCompat
import io.github.srdjanv.endreforked.compat.waila.WailaCompat
import io.github.srdjanv.endreforked.utils.Initializer
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.event.*

class CompatManger private constructor() : Initializer {
    interface ModCompat : Initializer

    // Check the mod id before construction: a compat class must not load when its mod is missing
    private val mods: List<ModCompat> = listOf<Pair<String, () -> ModCompat>>(
        "baubles" to { BaublesCompat() },
        "tconstruct" to { TConstruct() },
        "conarm" to { ConArm() },
        "theoneprobe" to { TheOneProbeCompat() },
        "waila" to { WailaCompat() },
    ).filter { Loader.isModLoaded(it.first) && (it.first != "conarm" || Loader.isModLoaded("tconstruct")) }.map { it.second() }

    override fun registerEventBus() = mods.forEach { it.registerEventBus() }
    override fun preInit(event: FMLPreInitializationEvent) = mods.forEach { it.preInit(event) }
    override fun init(event: FMLInitializationEvent) = mods.forEach { it.init(event) }
    override fun postInit(event: FMLPostInitializationEvent) = mods.forEach { it.postInit(event) }
    override fun onServerStarted(event: FMLServerStartedEvent) = mods.forEach { it.onServerStarted(event) }
    override fun onServerStopped(event: FMLServerStoppedEvent) = mods.forEach { it.onServerStopped(event) }
    override fun dispose() = true
    override fun onDispose() = mods.forEach { it.onDispose() }

    companion object {
        private var instance: CompatManger? = null

        @JvmStatic
        fun getInstance(): CompatManger = instance ?: CompatManger().also { instance = it }

        @JvmStatic
        fun invalidate() {
            instance = null
        }
    }
}
