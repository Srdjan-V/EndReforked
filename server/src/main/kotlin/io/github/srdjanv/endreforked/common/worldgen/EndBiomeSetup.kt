package io.github.srdjanv.endreforked.common.worldgen

import git.jbredwards.nether_api.api.event.NetherAPIRegistryEvent
import git.jbredwards.nether_api.mod.common.registry.NetherAPIRegistry
import io.github.srdjanv.endreforked.api.worldgen.EndBiome
import io.github.srdjanv.endreforked.common.registry.GameAdapter.toLocation
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import io.github.srdjanv.endreforked.impl.worldgen.EndBiomes
import net.minecraft.world.biome.Biome
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Logger

/**
 * Puts the enabled biomes of the registry into the End registry of Nether API.
 * Nether API clears its registries when a server stops and fires [NetherAPIRegistryEvent.End] again, so the biomes are applied on that event.
 * The first apply (init) covers the time before the event. Registering a biome again only replaces its weight.
 * A rebuild of the registry while the game runs does not change the biomes of the running game.
 */
class EndBiomeSetup(private val registry: ManagedRegistry<EndBiome, EndBiomes>, private val logger: Logger) {
    fun apply() {
        for (entry in registry.current) {
            if (!entry.enabled) continue
            val biome = Biome.REGISTRY.getObject(entry.id.toLocation()) ?: continue
            NetherAPIRegistry.THE_END.registerBiome(biome, entry.weight)
        }
    }

    @SubscribeEvent
    fun onEndRegistry(event: NetherAPIRegistryEvent.End) = apply()

    /** Call it after the first rebuild. */
    fun watchChanges() {
        registry.addListener { logger.warn("end_biomes was rebuilt. This changes the End biomes only at the next server start, not in the running game.") }
    }
}
