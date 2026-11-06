package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.entropy.CriticalEvent
import io.github.srdjanv.endreforked.api.registry.LoadReport
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.WandConversion
import io.github.srdjanv.endreforked.api.worldgen.EndBiome
import io.github.srdjanv.endreforked.api.worldgen.WorldFeature
import io.github.srdjanv.endreforked.impl.machine.MachineRecipes
import io.github.srdjanv.endreforked.impl.machine.toSnapshot
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import io.github.srdjanv.endreforked.impl.world.CriticalEvents
import io.github.srdjanv.endreforked.impl.world.FluidBlockInteractions
import io.github.srdjanv.endreforked.impl.world.FluidEntityInteractions
import io.github.srdjanv.endreforked.impl.world.WandConversions
import io.github.srdjanv.endreforked.impl.world.toSnapshot
import io.github.srdjanv.endreforked.impl.worldgen.EndBiomes
import io.github.srdjanv.endreforked.impl.worldgen.WorldFeatures
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import net.minecraft.world.biome.Biome

/**
 * All registries of one side. The server and the client each have their own instance, also in single player.
 * A new registry is added here: a property and a [Slot] in [slots]. A synced one also needs one case in the client packet handler.
 * The world features, the End biomes and the critical events are server only: nothing on the client reads them, so they have no snapshot,
 * and a client instance ([withWorldGen] false) leaves them out of [slots] and never builds them.
 */
class Registries(
    val machineRecipes: ManagedRegistry<MachineRecipe, MachineRecipes>,
    val wandConversions: ManagedRegistry<WandConversion, WandConversions>,
    val fluidBlockInteractions: ManagedRegistry<FluidBlockInteraction, FluidBlockInteractions>,
    val fluidEntityInteractions: ManagedRegistry<FluidEntityInteraction, FluidEntityInteractions>,
    val worldFeatures: ManagedRegistry<WorldFeature, WorldFeatures>,
    val endBiomes: ManagedRegistry<EndBiome, EndBiomes>,
    val criticalEvents: ManagedRegistry<CriticalEvent, CriticalEvents>,
    withWorldGen: Boolean,
) {
    /**
     * One registry without its types, for what all registries share. [name] is the path of the registry id.
     * A registry that is not synced to clients has no [snapshot] and no [onSnapshot].
     */
    class Slot(
        val name: String,
        val rebuild: () -> LoadReport,
        val snapshot: (() -> RegistrySnapshot)? = null,
        val onSnapshot: (((RegistrySnapshot) -> Unit) -> Unit)? = null,
    )

    val slots = listOf(
        Slot(machineRecipes.key.path, machineRecipes::rebuild, { machineRecipes.current.toSnapshot() }) { l ->
            machineRecipes.addListener { l(it.toSnapshot()) }
        },
        Slot(wandConversions.key.path, wandConversions::rebuild, { wandConversions.current.toSnapshot() }) { l ->
            wandConversions.addListener { l(it.toSnapshot()) }
        },
        Slot(fluidBlockInteractions.key.path, fluidBlockInteractions::rebuild, { fluidBlockInteractions.current.toSnapshot() }) { l ->
            fluidBlockInteractions.addListener { l(it.toSnapshot()) }
        },
        Slot(fluidEntityInteractions.key.path, fluidEntityInteractions::rebuild, { fluidEntityInteractions.current.toSnapshot() }) { l ->
            fluidEntityInteractions.addListener { l(it.toSnapshot()) }
        },
    ) + if (withWorldGen) listOf(
        Slot(worldFeatures.key.path, worldFeatures::rebuild),
        Slot(endBiomes.key.path, endBiomes::rebuild),
        Slot(criticalEvents.key.path, criticalEvents::rebuild),
    ) else emptyList()

    /** The current content of every synced registry, ready to send to a client. */
    fun snapshots(): List<RegistrySnapshot> = slots.mapNotNull { it.snapshot?.invoke() }

    /** Applies the code defaults and the script layer of every registry. Call it in the init phase, when the items and fluids exist. */
    fun rebuildAll(): List<Pair<Slot, LoadReport>> = slots.map { it to it.rebuild() }

    companion object {
        /** The registries of one side. The server passes true, the client false. */
        fun create(withWorldGen: Boolean) = Registries(
            MachineRecipes.managed(MachineRecipeDefaults::all),
            WandConversions.managed(WorldInteractionDefaults::wandConversions),
            FluidBlockInteractions.managed(WorldInteractionDefaults::fluidBlockInteractions),
            FluidEntityInteractions.managed(WorldInteractionDefaults::fluidEntityInteractions),
            WorldFeatures.managed(WorldFeatureDefaults::all, ::validate),
            EndBiomes.managed(WorldFeatureDefaults::endBiomes, ::validate),
            CriticalEvents.managed(CriticalEventDefaults::all),
            withWorldGen,
        )

        private fun validate(f: WorldFeature): String? =
            (f.biomes.allow.keys + f.biomes.deny).firstOrNull { !Biome.REGISTRY.containsKey(it) }?.let { "unknown biome $it" }

        private fun validate(b: EndBiome): String? = if (Biome.REGISTRY.containsKey(b.id)) null else "unknown biome ${b.id}"
    }
}
