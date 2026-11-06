package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.impl.registry.decodeEntries
import io.github.srdjanv.endreforked.impl.registry.toFluidStack
import io.github.srdjanv.endreforked.impl.registry.toItemStack
import io.github.srdjanv.endreforked.impl.registry.toState
import io.github.srdjanv.endreforked.impl.registry.toWire
import io.github.srdjanv.endreforked.wire.s2c.Aisle
import io.github.srdjanv.endreforked.wire.s2c.DropItemAction
import io.github.srdjanv.endreforked.wire.s2c.Ingredient
import io.github.srdjanv.endreforked.wire.s2c.OreIngredient
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import io.github.srdjanv.endreforked.wire.s2c.SetBlockAction
import io.github.srdjanv.endreforked.wire.s2c.SymbolSpec
import io.github.srdjanv.endreforked.wire.s2c.WorldAction as WireAction
import io.github.srdjanv.endreforked.wire.s2c.StructureSpec as WireStructure
import io.github.srdjanv.endreforked.wire.s2c.WorldEvent as WireEvent
import io.github.srdjanv.endreforked.wire.s2c.MachineRecipe as WireRecipe
import io.github.srdjanv.endreforked.wire.s2c.MachineRecipes as WireRecipes
import net.minecraft.util.ResourceLocation

/** Mapping between machine recipes and their [RegistrySnapshot] message. */
fun MachineRecipes.toSnapshot() = RegistrySnapshot(
    registry = key.toString(),
    machine_recipes = WireRecipes(entries.map { it.toWire() }),
)

/** Returns null when [this] is not a machine recipe snapshot. A recipe with something this client does not have is skipped. */
fun RegistrySnapshot.toMachineRecipes(): List<MachineRecipe>? =
    machine_recipes?.recipes?.decodeEntries({ it.id }) { it.toRecipe() }

private fun MachineRecipe.toWire() = WireRecipe(
    id = id.toString(),
    machine = machine.toString(),
    item_inputs = itemInputs.map {
        when (it) {
            is ItemIngredient.Item -> Ingredient(item = it.stack.toWire())
            is ItemIngredient.OreDict -> Ingredient(ore = OreIngredient(it.name, it.count))
        }
    },
    fluid_inputs = fluidInputs.map { it.toWire() },
    item_outputs = itemOutputs.map { it.toWire() },
    fluid_outputs = fluidOutputs.map { it.toWire() },
    ticks = ticks,
    entropy = entropy,
    priority = priority,
    world_events = worldEvents.map { it.toWire() },
)

private fun WorldEvent.toWire() = WireEvent(
    at_percent = atPercent,
    chance = chance,
    structure = WireStructure(
        aisles = structure.aisles.map { Aisle(it) },
        symbols = structure.symbols.map { (c, b) -> SymbolSpec(c.toString(), b.toWire()) },
    ),
    action = when (val a = action) {
        is WorldAction.SetBlock -> WireAction(set_block = SetBlockAction(a.block.toWire()))
        is WorldAction.DropItem -> WireAction(drop_item = DropItemAction(a.item.toWire()))
    },
)

private fun WireRecipe.toRecipe() = MachineRecipe(
    id = ResourceLocation(id),
    machine = ResourceLocation(machine),
    itemInputs = item_inputs.map {
        val item = it.item
        val ore = it.ore
        when {
            item != null -> ItemIngredient.Item(item.toItemStack())
            ore != null -> ItemIngredient.OreDict(ore.name, ore.count)
            else -> throw IllegalArgumentException("empty ingredient")
        }
    },
    fluidInputs = fluid_inputs.map { it.toFluidStack() },
    itemOutputs = item_outputs.map { it.toItemStack() },
    fluidOutputs = fluid_outputs.map { it.toFluidStack() },
    ticks = ticks,
    entropy = entropy,
    priority = priority,
    worldEvents = world_events.map { it.toEvent() },
)

private fun WireEvent.toEvent(): WorldEvent {
    val s = structure ?: throw IllegalArgumentException("world event without structure")
    val a = action ?: throw IllegalArgumentException("world event without action")
    val set = a.set_block
    val drop = a.drop_item
    return WorldEvent(
        atPercent = at_percent,
        chance = chance,
        structure = StructureSpec(
            aisles = s.aisles.map { it.rows },
            symbols = s.symbols.associate { sym ->
                require(sym.symbol.length == 1) { "symbol '${sym.symbol}' is not one character" }
                sym.symbol[0] to (sym.block ?: throw IllegalArgumentException("symbol without block")).toState()
            },
        ),
        action = when {
            set != null -> WorldAction.SetBlock((set.block ?: throw IllegalArgumentException("set_block without block")).toState())
            drop != null -> WorldAction.DropItem((drop.item ?: throw IllegalArgumentException("drop_item without item")).toItemStack())
            else -> throw IllegalArgumentException("empty world action")
        },
    )
}
