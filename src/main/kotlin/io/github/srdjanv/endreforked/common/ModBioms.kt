package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.common.bioms.EntropyBiome
import io.github.srdjanv.endreforked.common.bioms.OrganaBiome
import io.github.srdjanv.endreforked.common.bioms.base.BiomeDictionaryHandler
import net.minecraft.world.biome.Biome
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ModBioms {
    private val BIOMES = DeferredRegistry(this, Biome::class.java)

    @get:JvmStatic val ORGANA_BIOME by BIOMES.register(::OrganaBiome)
    @get:JvmStatic val ENTROPY_BIOME by BIOMES.register(::EntropyBiome)

    @SubscribeEvent
    @JvmStatic
    fun onBiomeRegister(event: RegistryEvent.Register<Biome>) {
        BIOMES.registerAll(event.registry)
        BIOMES.all().filterIsInstance<BiomeDictionaryHandler>().forEach { it.registerToBiomeDictionary() }
    }
}
