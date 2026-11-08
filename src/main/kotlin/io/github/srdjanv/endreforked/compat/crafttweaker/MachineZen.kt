package io.github.srdjanv.endreforked.compat.crafttweaker

import crafttweaker.annotations.ZenRegister
import crafttweaker.api.item.IIngredient
import crafttweaker.api.item.IItemStack
import crafttweaker.api.item.IngredientStack
import crafttweaker.api.liquid.ILiquidStack
import crafttweaker.api.minecraft.CraftTweakerMC
import crafttweaker.api.oredict.IOreDictEntry
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.common.registry.MachineRecipeDefaults
import net.minecraft.util.ResourceLocation
import stanhebben.zenscript.annotations.Optional
import stanhebben.zenscript.annotations.ZenClass
import stanhebben.zenscript.annotations.ZenMethod

// One ZenScript class per machine of the machine recipe registry. All three share the script layer, so each one only touches the recipes of its machine.
// An item input is one item (with a count) or one ore dictionary entry (with a count). The world events of the Materializer are not supported.

/** `mods.endreborn.EntropyChamber` */
@ZenRegister
@ZenClass("mods.endreborn.EntropyChamber")
object EntropyChamberZen {
    private val machine = MachineZen(MachineRecipeDefaults.ENTROPY_CHAMBER)

    /** Adds a recipe. Without [name] it gets `crafttweaker:entropy_chamber_N`. */
    @JvmStatic
    @ZenMethod
    fun addRecipe(
        inputs: Array<IIngredient>, outputs: Array<IItemStack>, ticks: Int,
        @Optional fluidInputs: Array<ILiquidStack>?, @Optional fluidOutputs: Array<ILiquidStack>?,
        @Optional entropy: Int, @Optional priority: Int, @Optional name: String?,
    ) = machine.addRecipe(inputs, outputs, ticks, fluidInputs, fluidOutputs, entropy, priority, name)

    /** Removes the recipe with this id, for example `endreborn:entropy_chamber/obsidian`. */
    @JvmStatic @ZenMethod fun remove(id: String) = machine.remove(id)

    @JvmStatic @ZenMethod fun removeAll() = machine.removeAll()

    @JvmStatic @ZenMethod fun removeByInput(input: IIngredient) = machine.removeByInput(input)

    @JvmStatic @ZenMethod fun removeByInput(input: ILiquidStack) = machine.removeByInput(input)

    @JvmStatic @ZenMethod fun removeByOutput(output: IIngredient) = machine.removeByOutput(output)

    @JvmStatic @ZenMethod fun removeByOutput(output: ILiquidStack) = machine.removeByOutput(output)
}

/** `mods.endreborn.EndForge` */
@ZenRegister
@ZenClass("mods.endreborn.EndForge")
object EndForgeZen {
    private val machine = MachineZen(MachineRecipeDefaults.END_FORGE)

    @JvmStatic
    @ZenMethod
    fun addRecipe(
        inputs: Array<IIngredient>, outputs: Array<IItemStack>, ticks: Int,
        @Optional fluidInputs: Array<ILiquidStack>?, @Optional fluidOutputs: Array<ILiquidStack>?,
        @Optional entropy: Int, @Optional priority: Int, @Optional name: String?,
    ) = machine.addRecipe(inputs, outputs, ticks, fluidInputs, fluidOutputs, entropy, priority, name)

    @JvmStatic @ZenMethod fun remove(id: String) = machine.remove(id)

    @JvmStatic @ZenMethod fun removeAll() = machine.removeAll()

    @JvmStatic @ZenMethod fun removeByInput(input: IIngredient) = machine.removeByInput(input)

    @JvmStatic @ZenMethod fun removeByInput(input: ILiquidStack) = machine.removeByInput(input)

    @JvmStatic @ZenMethod fun removeByOutput(output: IIngredient) = machine.removeByOutput(output)

    @JvmStatic @ZenMethod fun removeByOutput(output: ILiquidStack) = machine.removeByOutput(output)
}

/** `mods.endreborn.Materializer` */
@ZenRegister
@ZenClass("mods.endreborn.Materializer")
object MaterializerZen {
    private val machine = MachineZen(MachineRecipeDefaults.MATERIALIZER)

    @JvmStatic
    @ZenMethod
    fun addRecipe(
        inputs: Array<IIngredient>, outputs: Array<IItemStack>, ticks: Int,
        @Optional fluidInputs: Array<ILiquidStack>?, @Optional fluidOutputs: Array<ILiquidStack>?,
        @Optional entropy: Int, @Optional priority: Int, @Optional name: String?,
    ) = machine.addRecipe(inputs, outputs, ticks, fluidInputs, fluidOutputs, entropy, priority, name)

    @JvmStatic @ZenMethod fun remove(id: String) = machine.remove(id)

    @JvmStatic @ZenMethod fun removeAll() = machine.removeAll()

    @JvmStatic @ZenMethod fun removeByInput(input: IIngredient) = machine.removeByInput(input)

    @JvmStatic @ZenMethod fun removeByInput(input: ILiquidStack) = machine.removeByInput(input)

    @JvmStatic @ZenMethod fun removeByOutput(output: IIngredient) = machine.removeByOutput(output)

    @JvmStatic @ZenMethod fun removeByOutput(output: ILiquidStack) = machine.removeByOutput(output)
}

/** The work of the three machine classes. */
internal class MachineZen(private val machine: ResourceLocation) {
    private val layer get() = CtLayers.machines
    private val label get() = machine.path

    fun addRecipe(
        inputs: Array<IIngredient>, outputs: Array<IItemStack>, ticks: Int,
        fluidInputs: Array<ILiquidStack>?, fluidOutputs: Array<ILiquidStack>?,
        entropy: Int, priority: Int, name: String?,
    ) {
        val items = inputs.map { toIngredient(it) ?: return fail("$label: an item input must be one item or one ore dictionary entry, got $it") }
        val fluidIn = fluidInputs.orEmpty().map { CraftTweakerMC.getLiquidStack(it).copy() }
        val itemOut = outputs.map { CraftTweakerMC.getItemStack(it).copy() }
        val fluidOut = fluidOutputs.orEmpty().map { CraftTweakerMC.getLiquidStack(it).copy() }
        if (items.isEmpty() && fluidIn.isEmpty()) return fail("$label: needs an item or a fluid input")
        if (itemOut.isEmpty() && fluidOut.isEmpty()) return fail("$label: needs an item or a fluid output")
        if (ticks < 1) return fail("$label: ticks must be at least 1, got $ticks")
        act("Adding an End Reforked $label recipe") {
            layer.add(name?.let(layer::parse), label) {
                MachineRecipe(it, machine, items, fluidIn, itemOut, fluidOut, ticks, entropy, priority)
            }
        }
    }

    fun remove(id: String) = act("Removing the End Reforked $label recipe $id") { layer.remove(layer.parse(id)) }

    fun removeAll() = removeWhere("all recipes") { true }

    /** Removes the recipes with this item or ore dictionary entry as an input. */
    fun removeByInput(input: IIngredient) = removeWhere("the recipes with the input $input") { r ->
        r.itemInputs.any {
            when (it) {
                is ItemIngredient.Item -> input.matches(CraftTweakerMC.getIItemStack(it.stack))
                is ItemIngredient.OreDict -> oreName(input) == it.name
            }
        }
    }

    fun removeByInput(input: ILiquidStack) = removeWhere("the recipes with the input $input") { r -> r.fluidInputs.any { it.fluid == fluidOf(input) } }

    fun removeByOutput(output: IIngredient) = removeWhere("the recipes with the output $output") { r ->
        r.itemOutputs.any { output.matches(CraftTweakerMC.getIItemStack(it)) }
    }

    fun removeByOutput(output: ILiquidStack) = removeWhere("the recipes with the output $output") { r -> r.fluidOutputs.any { it.fluid == fluidOf(output) } }

    private fun removeWhere(what: String, test: (MachineRecipe) -> Boolean) =
        act("Removing $what of the End Reforked $label") { layer.removeIf { it.machine == machine && test(it) } }

    /** An ingredient without a count, so `<ore:x> * 2` is the entry `x`. */
    private fun base(ingredient: IIngredient) = (ingredient as? IngredientStack)?.internal as? IIngredient ?: ingredient

    private fun oreName(ingredient: IIngredient) = (base(ingredient) as? IOreDictEntry)?.name

    private fun toIngredient(ingredient: IIngredient): ItemIngredient? = when (val base = base(ingredient)) {
        is IOreDictEntry -> ItemIngredient.OreDict(base.name, ingredient.amount)
        is IItemStack -> ItemIngredient.Item(CraftTweakerMC.getItemStack(base).copy().also { it.count = ingredient.amount })
        else -> null
    }
}
