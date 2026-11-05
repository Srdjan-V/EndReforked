package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.impl.machine.MachineRecipes
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import io.github.srdjanv.endreforked.impl.machine.toSnapshot
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot

/**
 * All registries of one side. The server and the client each have their own instance, also in single player.
 * A new registry is added here and gets one case in [snapshots] and one in the client packet handler.
 */
class Registries(val machineRecipes: ManagedRegistry<MachineRecipe, MachineRecipes>) {
    /** The current content of every registry, ready to send to a client. */
    fun snapshots(): List<RegistrySnapshot> = listOf(machineRecipes.current.toSnapshot())

    /** Loads the code defaults. Call it in the init phase, when the items and fluids exist. The JSON layer is read later. */
    fun loadDefaults() {
        machineRecipes.reload(null)
    }

    companion object {
        fun create() = Registries(MachineRecipes.managed(MachineRecipeDefaults::all, GameAdapter::validate))
    }
}
