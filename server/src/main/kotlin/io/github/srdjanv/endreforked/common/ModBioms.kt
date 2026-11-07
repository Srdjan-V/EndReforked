package io.github.srdjanv.endreforked.common

import io.github.srdjanv.endreforked.common.bioms.EntropyBiome
import io.github.srdjanv.endreforked.common.bioms.OrganaBiome
import io.github.srdjanv.endreforked.common.bioms.base.BiomeDictionaryHandler
import net.minecraft.world.biome.Biome
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ModBioms {
    @JvmField val END_BIOMES = Registry<Biome>()

    @JvmField val ORGANA_BIOME: OrganaBiome = regEnd(::OrganaBiome)
    @JvmField val ENTROPY_BIOME: EntropyBiome = regEnd(::EntropyBiome)

    private fun <B : Biome> regEnd(factory: () -> B): B =
        factory().also { END_BIOMES.register(it) }

    @SubscribeEvent
    @JvmStatic
    fun onBiomeRegister(event: RegistryEvent.Register<Biome>) {
        for (biome in END_BIOMES.all()) {
            event.registry.register(biome)
            if (biome is BiomeDictionaryHandler) biome.registerToBiomeDictionary()
        }
    }
}
