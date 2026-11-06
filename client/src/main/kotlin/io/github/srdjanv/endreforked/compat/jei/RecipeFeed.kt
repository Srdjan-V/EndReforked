package io.github.srdjanv.endreforked.compat.jei

import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.api.registry.Registry
import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import io.github.srdjanv.endreforked.impl.machine.MachineRecipes
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import mezz.jei.api.IRecipeRegistry
import mezz.jei.api.recipe.IRecipeWrapper

/**
 * The JEI recipes of one category. They follow the client registry, also at runtime.
 * [build] makes the recipe wrappers of one registry snapshot.
 */
class RecipeFeed<E : RegistryEntry, R : Registry<E>>(private val uid: String, private val build: (R) -> List<IRecipeWrapper>) {
    private var shown: List<IRecipeWrapper> = emptyList()
    private var watched: Pair<ManagedRegistry<E, R>, (R) -> Unit>? = null

    /** The recipes for the first JEI load. */
    fun load(snapshot: R): List<IRecipeWrapper> = build(snapshot).also { shown = it }

    /**
     * From now on a new snapshot of [registry] replaces the recipes of this category in the running JEI.
     * JEI can start again (resource reload), so the old watch is dropped first.
     */
    fun watch(jei: IRecipeRegistry, registry: ManagedRegistry<E, R>) {
        watched?.let { (old, listener) -> old.removeListener(listener) }
        val listener = { snapshot: R ->
            shown.forEach { jei.removeRecipe(it, uid) }
            shown = build(snapshot)
            shown.forEach { jei.addRecipe(it, uid) }
        }
        registry.addListener(listener)
        watched = registry to listener
    }

    companion object {
        /** The feed of the recipes of one machine. */
        fun machine(uid: String, machine: Id) = RecipeFeed<MachineRecipe, MachineRecipes>(uid) { snapshot ->
            snapshot.forMachine(machine).mapNotNull(MachineRecipeWrapper::of)
        }
    }
}
