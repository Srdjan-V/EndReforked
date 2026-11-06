package io.github.srdjanv.endreforked.common

import com.google.common.base.Suppliers
import io.github.srdjanv.endreforked.common.bioms.EntropyBiome
import io.github.srdjanv.endreforked.common.bioms.OrganaBiome
import io.github.srdjanv.endreforked.common.bioms.base.BiomeDictionaryHandler
import net.minecraft.world.biome.Biome
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.function.Supplier

object ModBioms {
    @JvmField val END_BIOMES = mutableListOf<Supplier<out Biome>>()

    @JvmField val ORGANA_BIOME: Supplier<OrganaBiome> = regEnd(::OrganaBiome)
    @JvmField val ENTROPY_BIOME: Supplier<EntropyBiome> = regEnd(::EntropyBiome)

    private fun <B : Biome> regEnd(factory: () -> B): Supplier<B> =
        Suppliers.memoize { factory() }.also { END_BIOMES += it }

    @SubscribeEvent
    @JvmStatic
    fun onBiomeRegister(event: RegistryEvent.Register<Biome>) {
        for (supplier in END_BIOMES) {
            val biome = supplier.get()
            event.registry.register(biome)
            if (biome is BiomeDictionaryHandler) biome.registerToBiomeDictionary()
        }
    }
}
