package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.FluidIngredient
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WILDCARD_META
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.api.registry.RegistryKey
import io.github.srdjanv.endreforked.impl.registry.FrozenRegistry
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry

/** An item type without count. */
data class ItemKey(val id: Id, val meta: Int)

/** The stack in one input slot. An empty slot is null. */
data class ItemInput(val key: ItemKey, val count: Int)

/** The fluid in one input tank. An empty tank is null. */
data class FluidInput(val id: Id, val amount: Int)

/** Tells if an item is in an ore dictionary entry. The caller supplies it, so this module needs no Minecraft classes. */
fun interface OreResolver {
    fun matches(key: ItemKey, ore: String): Boolean

    companion object {
        val NONE = OreResolver { _, _ -> false }
    }
}

/**
 * A found recipe. [itemsTaken] and [fluidsTaken] are how much to remove from each input slot or tank, by position.
 * They are what the machine must extract, all or nothing.
 */
class Match(val recipe: MachineRecipe, val itemsTaken: IntArray, val fluidsTaken: IntArray)

/**
 * A snapshot of all machine recipes with the lookup of this registry.
 * Recipes are indexed by machine, then by the id of their first concrete ingredient.
 */
class MachineRecipes(entries: Collection<MachineRecipe>) : FrozenRegistry<MachineRecipe>(KEY, entries) {
    private class Index {
        val all = ArrayList<MachineRecipe>()
        val byItem = HashMap<Id, MutableList<MachineRecipe>>()
        val byFluid = HashMap<Id, MutableList<MachineRecipe>>()
        val other = ArrayList<MachineRecipe>() // only ore dictionary ingredients
    }

    private val machines = HashMap<Id, Index>()

    init {
        for (recipe in this.entries) {
            val index = machines.getOrPut(recipe.machine, ::Index)
            index.all += recipe
            val item = recipe.itemInputs.firstNotNullOfOrNull { it as? ItemIngredient.Item }
            val fluid = recipe.fluidInputs.firstOrNull()
            when {
                item != null -> index.byItem.getOrPut(item.id, ::ArrayList) += recipe
                fluid != null -> index.byFluid.getOrPut(fluid.id, ::ArrayList) += recipe
                else -> index.other += recipe
            }
        }
    }

    fun forMachine(machine: Id): List<MachineRecipe> = machines[machine]?.all ?: emptyList()

    /** True if some recipe of [machine] has an item input that [key] fits. Machines use it to reject useless input. */
    fun acceptsItem(machine: Id, key: ItemKey, ores: OreResolver): Boolean =
        forMachine(machine).any { r -> r.itemInputs.any { matches(it, key, ores) } }

    fun acceptsFluid(machine: Id, fluid: Id): Boolean =
        forMachine(machine).any { r -> r.fluidInputs.any { it.id == fluid } }

    /**
     * Finds the recipe of [machine] that the inputs satisfy. Input has to hold at least the counts and amounts of the recipe.
     * Extra input is allowed. A recipe is only found if all its inputs are satisfied by the given slots and tanks,
     * so a machine part with no fluid tanks never finds a recipe with fluid inputs.
     * With several matches the highest priority wins, then the lowest id.
     */
    fun find(
        machine: Id,
        items: List<ItemInput?>,
        fluids: List<FluidInput?>,
        ores: OreResolver = OreResolver.NONE,
    ): Match? {
        val index = machines[machine] ?: return null
        val candidates = LinkedHashSet<MachineRecipe>()
        for (item in items) item?.let { index.byItem[it.key.id]?.let(candidates::addAll) }
        for (fluid in fluids) fluid?.let { index.byFluid[it.id]?.let(candidates::addAll) }
        candidates += index.other
        return candidates.sortedWith(ORDER).firstNotNullOfOrNull { match(it, items, fluids, ores) }
    }

    // ponytail: greedy, concrete items before ore entries. A set of overlapping ingredients that needs a smarter split is not found.
    private fun match(r: MachineRecipe, items: List<ItemInput?>, fluids: List<FluidInput?>, ores: OreResolver): Match? {
        val itemLeft = IntArray(items.size) { items[it]?.count ?: 0 }
        val itemTaken = IntArray(items.size)
        for (ing in r.itemInputs.filterIsInstance<ItemIngredient.Item>() + r.itemInputs.filterIsInstance<ItemIngredient.OreDict>()) {
            var need = ing.count
            for (i in items.indices) {
                val stack = items[i] ?: continue
                if (itemLeft[i] == 0 || !matches(ing, stack.key, ores)) continue
                val take = minOf(need, itemLeft[i])
                itemLeft[i] -= take
                itemTaken[i] += take
                need -= take
                if (need == 0) break
            }
            if (need > 0) return null
        }

        val fluidLeft = IntArray(fluids.size) { fluids[it]?.amount ?: 0 }
        val fluidTaken = IntArray(fluids.size)
        for (ing: FluidIngredient in r.fluidInputs) {
            var need = ing.amount
            for (i in fluids.indices) {
                if (fluidLeft[i] == 0 || fluids[i]?.id != ing.id) continue
                val take = minOf(need, fluidLeft[i])
                fluidLeft[i] -= take
                fluidTaken[i] += take
                need -= take
                if (need == 0) break
            }
            if (need > 0) return null
        }
        return Match(r, itemTaken, fluidTaken)
    }

    private fun matches(ing: ItemIngredient, key: ItemKey, ores: OreResolver) = when (ing) {
        is ItemIngredient.Item -> ing.id == key.id && (ing.meta == WILDCARD_META || ing.meta == key.meta)
        is ItemIngredient.OreDict -> ores.matches(key, ing.name)
    }

    companion object {
        val KEY = RegistryKey<MachineRecipe>(Id("endreborn", "machine_recipes"))

        private val ORDER = compareByDescending<MachineRecipe> { it.priority }.thenBy { it.id }

        /** The managed registry for machine recipes. [extraValidation] adds checks that need the game (does the item exist). */
        fun managed(
            defaults: () -> Collection<MachineRecipe>,
            extraValidation: (MachineRecipe) -> String? = { null },
        ) = ManagedRegistry(
            KEY, defaults, MachineRecipeJson, { validate(it) ?: extraValidation(it) }, ::MachineRecipes,
        )

        /** Returns a message when the recipe cannot work, else null. */
        fun validate(r: MachineRecipe): String? = when {
            r.itemInputs.isEmpty() && r.fluidInputs.isEmpty() -> "no inputs"
            r.itemOutputs.isEmpty() && r.fluidOutputs.isEmpty() -> "no outputs"
            r.ticks < 1 -> "ticks must be at least 1"
            r.entropy < 0 -> "entropy must not be negative"
            r.itemInputs.any { it.count < 1 } -> "item input count must be at least 1"
            r.itemInputs.any { it is ItemIngredient.Item && it.meta !in 0..WILDCARD_META } -> "item meta out of range"
            r.itemInputs.any { it is ItemIngredient.OreDict && it.name.isBlank() } -> "ore dictionary name is empty"
            r.fluidInputs.any { it.amount < 1 } -> "fluid input amount must be at least 1"
            r.itemOutputs.any { it.count < 1 || it.meta !in 0..WILDCARD_META } -> "item output count or meta is invalid"
            r.fluidOutputs.any { it.amount < 1 } -> "fluid output amount must be at least 1"
            else -> validateWorldEvents(r.worldEvents)
        }

        private fun validateWorldEvents(events: List<WorldEvent>): String? {
            if (events.map { it.atPercent }.toSet().size != events.size) return "two world events at the same percent"
            for (e in events) {
                val at = "world event at ${e.atPercent}%"
                if (e.atPercent !in 1..100) return "$at: atPercent must be 1 to 100"
                if (e.chance !in 0..100) return "$at: chance must be 0 to 100"
                structureProblem(e.structure)?.let { return "$at: $it" }
                val a = e.action
                if (a is WorldAction.DropItem && (a.item.count < 1 || a.item.meta !in 0..WILDCARD_META)) return "$at: drop count or meta is invalid"
            }
            return null
        }

        private fun structureProblem(s: StructureSpec): String? {
            val rows = s.aisles.firstOrNull()?.size ?: 0
            val width = s.aisles.firstOrNull()?.firstOrNull()?.length ?: 0
            if (rows == 0 || width == 0) return "structure is empty"
            if (s.aisles.any { it.size != rows || it.any { row -> row.length != width } }) return "structure rows differ in size"
            if (StructureSpec.ANY in s.symbols) return "the blank cannot be mapped to a block"
            val used = s.aisles.flatMap { a -> a.flatMap { it.toList() } }.toSet() - StructureSpec.ANY
            if (used.isEmpty()) return "structure has only blanks"
            used.firstOrNull { it !in s.symbols }?.let { return "symbol '$it' has no block" }
            return null
        }
    }
}
