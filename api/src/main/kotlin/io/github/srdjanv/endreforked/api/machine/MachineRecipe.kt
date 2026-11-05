package io.github.srdjanv.endreforked.api.machine

import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.api.registry.RegistryEntry

/** Item metadata that matches every damage value. Same value as Forge's OreDictionary.WILDCARD_VALUE. */
const val WILDCARD_META = 32767

/** An item input. Counts are the minimum that must be present, and they are consumed. */
sealed interface ItemIngredient {
    val count: Int

    data class Item(val id: Id, val meta: Int = WILDCARD_META, override val count: Int = 1) : ItemIngredient

    data class OreDict(val name: String, override val count: Int = 1) : ItemIngredient
}

data class FluidIngredient(val id: Id, val amount: Int)

data class ItemOut(val id: Id, val meta: Int = 0, val count: Int = 1)

data class FluidOut(val id: Id, val amount: Int)

/**
 * One recipe of a machine. Pure data, no behaviour. NBT is not part of the model:
 * no current recipe depends on NBT.
 * Of several matching recipes, the highest [priority] wins, then the lowest [id].
 * [worldEvents] are for machines that change the world while they run. The others leave them empty.
 */
data class MachineRecipe(
    override val id: Id,
    val machine: Id,
    val itemInputs: List<ItemIngredient> = emptyList(),
    val fluidInputs: List<FluidIngredient> = emptyList(),
    val itemOutputs: List<ItemOut> = emptyList(),
    val fluidOutputs: List<FluidOut> = emptyList(),
    val ticks: Int,
    val entropy: Int = 0,
    val priority: Int = 0,
    val worldEvents: List<WorldEvent> = emptyList(),
) : RegistryEntry

/** The first world event after [percent] of progress, or null. */
fun MachineRecipe.nextWorldEvent(percent: Int): WorldEvent? =
    worldEvents.filter { it.atPercent > percent }.minByOrNull { it.atPercent }

typealias MachineRecipeScripts = io.github.srdjanv.endreforked.api.registry.RegistryScripts<MachineRecipe>

fun MachineRecipeScripts.removeByItemInput(item: Id) =
    removeIf { r -> r.itemInputs.any { it is ItemIngredient.Item && it.id == item } }

fun MachineRecipeScripts.removeByOreInput(ore: String) =
    removeIf { r -> r.itemInputs.any { it is ItemIngredient.OreDict && it.name == ore } }

fun MachineRecipeScripts.removeByFluidInput(fluid: Id) = removeIf { r -> r.fluidInputs.any { it.id == fluid } }

fun MachineRecipeScripts.removeByItemOutput(item: Id) = removeIf { r -> r.itemOutputs.any { it.id == item } }

fun MachineRecipeScripts.removeByFluidOutput(fluid: Id) = removeIf { r -> r.fluidOutputs.any { it.id == fluid } }

fun MachineRecipeScripts.removeByMachine(machine: Id) = removeIf { it.machine == machine }
