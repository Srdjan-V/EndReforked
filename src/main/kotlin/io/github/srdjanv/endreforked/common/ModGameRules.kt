package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.utils.Initializer
import net.minecraft.world.GameRules
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.event.FMLServerStartedEvent
import net.minecraftforge.fml.relauncher.FMLLaunchHandler

class ModGameRules : Initializer {
    override fun onServerStarted(event: FMLServerStartedEvent) {
        val rules = FMLCommonHandler.instance().minecraftServerInstance.getWorld(0).gameRules
        if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
            val key = Tags.MODID + "_dev_disable_region_loading"
            if (!rules.hasRule(key)) rules.addGameRule(key, "false", GameRules.ValueType.BOOLEAN_VALUE)
        }
    }
}
