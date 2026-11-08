package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.impl.registry.FrozenRegistry
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.oredict.OreDictionary

/**
 * A found recipe. [itemsTaken] and [fluidsTaken] are how much to remove from each input slot or tank, by position.
 * They are what the machine must extract, all or nothing.
 */
class Match(val recipe: MachineRecipe, val itemsTaken: IntArray, val fluidsTaken: IntArray)

/**
 * A snapshot of all machine recipes with the lookup of this registry.
 * Recipes are indexed by machine, then by the item or fluid of their first concrete ingredient.
 */
class MachineRecipes(entries: Collection<MachineRecipe>) : FrozenRegistry<MachineRecipe>(KEY, entries) {
    private class Index {
        val all = ArrayList<MachineRecipe>()
        val byItem = HashMap<Item, MutableList<MachineRecipe>>()
        val byFluid = HashMap<Fluid, MutableList<MachineRecipe>>()
        val other = ArrayList<MachineRecipe>() // only ore dictionary ingredients
    }

    private val machines = HashMap<ResourceLocation, Index>()

    init {
        for (recipe in this.entries) {
            val index = machines.getOrPut(recipe.machine, ::Index)
            index.all += recipe
            val item = recipe.itemInputs.firstNotNullOfOrNull { it as? ItemIngredient.Item }
            val fluid = recipe.fluidInputs.firstOrNull()
            when {
                item != null -> index.byItem.getOrPut(item.stack.item, ::ArrayList) += recipe
                fluid != null -> index.byFluid.getOrPut(fluid.fluid, ::ArrayList) += recipe
                else -> index.other += recipe
            }
        }
    }

    fun forMachine(machine: ResourceLocation): List<MachineRecipe> = machines[machine]?.all ?: emptyList()

    /** True if some recipe of [machine] has an item input that [stack] fits. Machines use it to reject useless input. */
    fun acceptsItem(machine: ResourceLocation, stack: ItemStack): Boolean =
        forMachine(machine).any { r -> r.itemInputs.any { matches(it, stack) } }

    fun acceptsFluid(machine: ResourceLocation, fluid: Fluid): Boolean =
        forMachine(machine).any { r -> r.fluidInputs.any { it.fluid == fluid } }

    /**
     * Finds the recipe of [machine] that the inputs satisfy. Input has to hold at least the counts and amounts of the recipe.
     * Extra input is allowed. A recipe is only found if all its inputs are satisfied by the given slots and tanks,
     * so a machine part with no fluid tanks never finds a recipe with fluid inputs.
     * With several matches the highest priority wins, then the lowest id.
     * An empty slot is an empty stack, and an empty tank is null.
     */
    fun find(machine: ResourceLocation, items: List<ItemStack>, fluids: List<FluidStack?>): Match? {
        val index = machines[machine] ?: return null
        val candidates = LinkedHashSet<MachineRecipe>()
        for (stack in items) index.byItem[stack.item]?.let(candidates::addAll)
        for (fluid in fluids) fluid?.let { index.byFluid[it.fluid]?.let(candidates::addAll) }
        candidates += index.other
        return candidates.sortedWith(ORDER).firstNotNullOfOrNull { match(it, items, fluids) }
    }

    // ponytail: greedy, concrete items before ore entries. A set of overlapping ingredients that needs a smarter split is not found.
    private fun match(r: MachineRecipe, items: List<ItemStack>, fluids: List<FluidStack?>): Match? {
        val itemLeft = IntArray(items.size) { if (items[it].isEmpty) 0 else items[it].count }
        val itemTaken = IntArray(items.size)
        for (ing in r.itemInputs.filterIsInstance<ItemIngredient.Item>() + r.itemInputs.filterIsInstance<ItemIngredient.OreDict>()) {
            var need = ing.count
            for (i in items.indices) {
                if (itemLeft[i] == 0 || !matches(ing, items[i])) continue
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
        for (ing in r.fluidInputs) {
            var need = ing.amount
            for (i in fluids.indices) {
                if (fluidLeft[i] == 0 || fluids[i]?.fluid != ing.fluid) continue
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

    private fun matches(ing: ItemIngredient, stack: ItemStack) = when (ing) {
        is ItemIngredient.Item -> OreDictionary.itemMatches(ing.stack, stack, false) // exact meta, or the wildcard meta of the ingredient
        is ItemIngredient.OreDict -> OreDictionary.getOreIDs(stack).any { OreDictionary.getOreName(it) == ing.name }
    }

    companion object {
        val KEY = ResourceLocation("endreborn", "machine_recipes")

        private val ORDER = compareByDescending<MachineRecipe> { it.priority }.thenBy { it.id }

        fun managed(defaults: () -> Collection<MachineRecipe>) = ManagedRegistry(KEY, defaults, ::validate, ::MachineRecipes)

        /** Returns a message when the recipe cannot work, else null. */
        fun validate(r: MachineRecipe): String? = when {
            r.itemInputs.isEmpty() && r.fluidInputs.isEmpty() -> "no inputs"
            r.itemOutputs.isEmpty() && r.fluidOutputs.isEmpty() -> "no outputs"
            r.ticks < 1 -> "ticks must be at least 1"
            r.entropy < 0 -> "entropy must not be negative"
            r.itemInputs.any { it.count < 1 } -> "item input count must be at least 1"
            r.itemInputs.any { it is ItemIngredient.OreDict && it.name.isBlank() } -> "ore dictionary name is empty"
            r.fluidInputs.any { it.amount < 1 } -> "fluid input amount must be at least 1"
            r.itemOutputs.any { it.isEmpty } -> "item output is empty"
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
                if (a is WorldAction.DropItem && a.item.isEmpty) return "$at: dropped item is empty"
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
