package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.registry.LoadReport
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.WandConversion
import io.github.srdjanv.endreforked.impl.machine.MachineRecipes
import io.github.srdjanv.endreforked.impl.machine.toSnapshot
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import io.github.srdjanv.endreforked.impl.world.FluidBlockInteractions
import io.github.srdjanv.endreforked.impl.world.FluidEntityInteractions
import io.github.srdjanv.endreforked.impl.world.WandConversions
import io.github.srdjanv.endreforked.impl.world.toSnapshot
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot

/**
 * All registries of one side. The server and the client each have their own instance, also in single player.
 * A new registry is added here: a property, a [Slot] in [slots], and one case in the client packet handler.
 */
class Registries(
    val machineRecipes: ManagedRegistry<MachineRecipe, MachineRecipes>,
    val wandConversions: ManagedRegistry<WandConversion, WandConversions>,
    val fluidBlockInteractions: ManagedRegistry<FluidBlockInteraction, FluidBlockInteractions>,
    val fluidEntityInteractions: ManagedRegistry<FluidEntityInteraction, FluidEntityInteractions>,
) {
    /** One registry without its types, for what all registries share. [name] is the path of the registry id. */
    class Slot(
        val name: String,
        val rebuild: () -> LoadReport,
        val snapshot: () -> RegistrySnapshot,
        val onSnapshot: ((RegistrySnapshot) -> Unit) -> Unit,
    )

    val slots = listOf(
        Slot(machineRecipes.key.id.path, machineRecipes::rebuild, { machineRecipes.current.toSnapshot() }) { l ->
            machineRecipes.addListener { l(it.toSnapshot()) }
        },
        Slot(wandConversions.key.id.path, wandConversions::rebuild, { wandConversions.current.toSnapshot() }) { l ->
            wandConversions.addListener { l(it.toSnapshot()) }
        },
        Slot(fluidBlockInteractions.key.id.path, fluidBlockInteractions::rebuild, { fluidBlockInteractions.current.toSnapshot() }) { l ->
            fluidBlockInteractions.addListener { l(it.toSnapshot()) }
        },
        Slot(fluidEntityInteractions.key.id.path, fluidEntityInteractions::rebuild, { fluidEntityInteractions.current.toSnapshot() }) { l ->
            fluidEntityInteractions.addListener { l(it.toSnapshot()) }
        },
    )

    /** The current content of every registry, ready to send to a client. */
    fun snapshots(): List<RegistrySnapshot> = slots.map { it.snapshot() }

    /** Applies the code defaults and the script layer of every registry. Call it in the init phase, when the items and fluids exist. */
    fun rebuildAll(): List<Pair<Slot, LoadReport>> = slots.map { it to it.rebuild() }

    companion object {
        fun create() = Registries(
            MachineRecipes.managed(MachineRecipeDefaults::all, GameAdapter::validate),
            WandConversions.managed(WorldInteractionDefaults::wandConversions, GameAdapter::validate),
            FluidBlockInteractions.managed(WorldInteractionDefaults::fluidBlockInteractions, GameAdapter::validate),
            FluidEntityInteractions.managed(WorldInteractionDefaults::fluidEntityInteractions, GameAdapter::validate),
        )
    }
}
