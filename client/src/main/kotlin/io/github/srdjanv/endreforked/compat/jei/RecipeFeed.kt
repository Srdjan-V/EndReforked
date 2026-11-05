package io.github.srdjanv.endreforked.compat.jei

import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.impl.machine.MachineRecipes
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import mezz.jei.api.IRecipeRegistry

/** The JEI recipes of one machine category. They follow the client registry, also at runtime. */
class RecipeFeed(private val uid: String, private val machine: Id) {
    private var shown: List<MachineRecipeWrapper> = emptyList()
    private var watched: Pair<ManagedRegistry<MachineRecipe, MachineRecipes>, (MachineRecipes) -> Unit>? = null

    private fun build(snapshot: MachineRecipes) = snapshot.forMachine(machine).mapNotNull(MachineRecipeWrapper::of)

    /** The recipes for the first JEI load. */
    fun load(snapshot: MachineRecipes): List<MachineRecipeWrapper> = build(snapshot).also { shown = it }

    /**
     * From now on a new snapshot of [registry] replaces the recipes of this machine in the running JEI.
     * JEI can start again (resource reload), so the old watch is dropped first.
     */
    fun watch(jei: IRecipeRegistry, registry: ManagedRegistry<MachineRecipe, MachineRecipes>) {
        watched?.let { (old, listener) -> old.removeListener(listener) }
        val listener = { snapshot: MachineRecipes ->
            shown.forEach { jei.removeRecipe(it, uid) }
            shown = build(snapshot)
            shown.forEach { jei.addRecipe(it, uid) }
        }
        registry.addListener(listener)
        watched = registry to listener
    }
}
