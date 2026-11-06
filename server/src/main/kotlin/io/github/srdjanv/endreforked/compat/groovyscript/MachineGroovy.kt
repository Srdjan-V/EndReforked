package io.github.srdjanv.endreforked.compat.groovyscript

import com.cleanroommc.groovyscript.api.GroovyLog
import com.cleanroommc.groovyscript.api.IIngredient
import com.cleanroommc.groovyscript.helper.Alias
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder
import com.cleanroommc.groovyscript.helper.ingredient.OreDictIngredient
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.common.registry.MachineRecipeDefaults
import io.github.srdjanv.endreforked.compat.scripting.ScriptLayer
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.FluidStack

/**
 * One machine of the machine recipe registry: `mods.endreborn.entropy_chamber`, `end_forge` or `materializer`.
 * All three share the script layer of the registry, so each one only touches the recipes of its machine.
 */
class MachineGroovy(name: String, internal val machine: ResourceLocation, layer: ScriptLayer<MachineRecipe>) :
    ReloadableGroovy<MachineRecipe>(Alias.generateOf(name), layer) {

    fun recipeBuilder() = MachineRecipeBuilder(this)

    /** Removes the recipe with this id, for example `endreborn:end_forge/obsidian`. */
    fun remove(id: String) = layer.remove(layer.parse(id))

    fun removeAll() = removeWhere { true }

    /** Removes the recipes with this item or ore dictionary entry as an input. */
    fun removeByInput(input: IIngredient) = removeWhere { r ->
        r.itemInputs.any {
            when (it) {
                is ItemIngredient.Item -> input.test(it.stack)
                is ItemIngredient.OreDict -> input is OreDictIngredient && input.oreDict == it.name
            }
        }
    }

    fun removeByInput(input: FluidStack) = removeWhere { r -> r.fluidInputs.any { it.fluid == input.fluid } }

    fun removeByOutput(output: IIngredient) = removeWhere { r -> r.itemOutputs.any { output.test(it) } }

    fun removeByOutput(output: FluidStack) = removeWhere { r -> r.fluidOutputs.any { it.fluid == output.fluid } }

    private fun removeWhere(test: (MachineRecipe) -> Boolean) = layer.removeIf { it.machine == machine && test(it) }
}

class MachineRecipeBuilder(private val registry: MachineGroovy) : AbstractRecipeBuilder<MachineRecipe>() {
    private var ticks = 0
    private var entropy = 0
    private var priority = 0
    private val events = ArrayList<WorldEvent>()
    private val items = ArrayList<ItemIngredient>()

    fun ticks(ticks: Int) = apply { this.ticks = ticks }

    fun entropy(entropy: Int) = apply { this.entropy = entropy }

    fun priority(priority: Int) = apply { this.priority = priority }

    /**
     * A world event, for the Materializer. At [atPercent] of the progress it triggers in [chance] percent, and needs a match of [structure].
     * [structure] is a list of layers, each a list of rows. [symbols] maps each character of the rows to a block state (a blank matches any block).
     * The result replaces each matched block.
     */
    fun worldEvent(atPercent: Int, chance: Int, structure: List<*>, symbols: Map<*, *>, result: IBlockState) =
        event(atPercent, chance, structure, symbols, WorldAction.SetBlock(result))

    /** Like the other [worldEvent], but each matched block is removed and the item drops in its place. */
    fun worldEvent(atPercent: Int, chance: Int, structure: List<*>, symbols: Map<*, *>, drop: ItemStack) =
        event(atPercent, chance, structure, symbols, WorldAction.DropItem(drop.copy()))

    private fun event(atPercent: Int, chance: Int, structure: List<*>, symbols: Map<*, *>, action: WorldAction) = apply {
        val aisles = structure.map { layer -> (layer as List<*>).map { it.toString() } }
        events += WorldEvent(atPercent, chance, StructureSpec(aisles, symbols.entries.associate { it.key.toString().single() to it.value as IBlockState }), action)
    }

    override fun getErrorMsg() = "Error adding an End Reforked ${registry.machine.path} recipe"

    override fun validate(msg: GroovyLog.Msg) {
        validateItems(msg, 0, Int.MAX_VALUE, 0, Int.MAX_VALUE) // trims empty stacks, the counts are checked below
        validateFluids(msg, 0, Int.MAX_VALUE, 0, Int.MAX_VALUE)
        items.clear()
        for (ingredient in input) {
            val converted = toIngredient(ingredient)
            msg.add(converted == null, "an item input must be one item or one ore dictionary entry, got {}", ingredient)
            if (converted != null) items += converted
        }
        msg.add(items.isEmpty() && fluidInput.isEmpty(), "needs an item or a fluid input")
        msg.add(output.isEmpty() && fluidOutput.isEmpty(), "needs an item or a fluid output")
        msg.add(ticks < 1, "ticks must be at least 1, got {}", ticks)
        msg.add(events.isNotEmpty() && registry.machine != MachineRecipeDefaults.MATERIALIZER, "only the materializer has world events")
    }

    /** Fewer checks than the registry has: it validates the rest (counts, amounts, events) when it rebuilds, and the errors go to the log. */
    override fun register(): MachineRecipe? {
        if (!validate()) return null
        return registry.layer.add(name, registry.machine.path) {
            MachineRecipe(
                id = it,
                machine = registry.machine,
                itemInputs = items.toList(),
                fluidInputs = fluidInput.map { f -> f.copy() },
                itemOutputs = output.map { o -> o.copy() },
                fluidOutputs = fluidOutput.map { f -> f.copy() },
                ticks = ticks,
                entropy = entropy,
                priority = priority,
                worldEvents = events.toList(),
            )
        }
    }

    private fun toIngredient(ingredient: IIngredient): ItemIngredient? {
        if (ingredient is OreDictIngredient) return ItemIngredient.OreDict(ingredient.oreDict, ingredient.amount)
        val stacks = ingredient.matchingStacks
        if (stacks.size != 1) return null
        return ItemIngredient.Item(stacks[0].copy().also { it.count = ingredient.amount })
    }
}
