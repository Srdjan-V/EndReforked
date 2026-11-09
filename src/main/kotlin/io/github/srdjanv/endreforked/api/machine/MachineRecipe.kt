package io.github.srdjanv.endreforked.api.machine

import io.github.srdjanv.endreforked.api.entropy.EntropyLevel
import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import io.github.srdjanv.endreforked.api.registry.RegistryScripts
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidStack

/** An item input. Counts are the minimum that must be present, and they are consumed. */
sealed interface ItemIngredient {
    val count: Int

    /** The count of [stack] is the count needed. Its meta matches exactly, or every damage value with `OreDictionary.WILDCARD_VALUE`. NBT is ignored. */
    data class Item(val stack: ItemStack) : ItemIngredient {
        override val count get() = stack.count
    }

    data class OreDict(val name: String, override val count: Int = 1) : ItemIngredient
}

/**
 * One recipe of a machine. Pure data, no behaviour. The stacks are never changed: copy before you give one out.
 * NBT is not part of the model and is not synced to clients: no current recipe depends on NBT.
 * Of several matching recipes, the highest [priority] wins, then the lowest [id].
 * [worldEvents] are for machines that change the world while they run. The others leave them empty.
 * The recipe only runs when the chunk of the machine is at [minLevel] or higher.
 */
data class MachineRecipe(
    override val id: ResourceLocation,
    val machine: ResourceLocation,
    val itemInputs: List<ItemIngredient> = emptyList(),
    val fluidInputs: List<FluidStack> = emptyList(),
    val itemOutputs: List<ItemStack> = emptyList(),
    val fluidOutputs: List<FluidStack> = emptyList(),
    val ticks: Int,
    val entropy: Int = 0,
    val priority: Int = 0,
    val worldEvents: List<WorldEvent> = emptyList(),
    val minLevel: EntropyLevel = EntropyLevel.STABLE,
) : RegistryEntry

/** The first world event after [percent] of progress, or null. */
fun MachineRecipe.nextWorldEvent(percent: Int): WorldEvent? =
    worldEvents.filter { it.atPercent > percent }.minByOrNull { it.atPercent }

typealias MachineRecipeScripts = RegistryScripts<MachineRecipe>

fun MachineRecipeScripts.removeByItemInput(item: Item) =
    removeIf { r -> r.itemInputs.any { it is ItemIngredient.Item && it.stack.item == item } }

fun MachineRecipeScripts.removeByOreInput(ore: String) =
    removeIf { r -> r.itemInputs.any { it is ItemIngredient.OreDict && it.name == ore } }

fun MachineRecipeScripts.removeByFluidInput(fluid: Fluid) = removeIf { r -> r.fluidInputs.any { it.fluid == fluid } }

fun MachineRecipeScripts.removeByItemOutput(item: Item) = removeIf { r -> r.itemOutputs.any { it.item == item } }

fun MachineRecipeScripts.removeByFluidOutput(fluid: Fluid) = removeIf { r -> r.fluidOutputs.any { it.fluid == fluid } }

fun MachineRecipeScripts.removeByMachine(machine: ResourceLocation) = removeIf { it.machine == machine }
