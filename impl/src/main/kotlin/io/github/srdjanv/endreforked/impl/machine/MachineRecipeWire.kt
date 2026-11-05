package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.FluidIngredient
import io.github.srdjanv.endreforked.api.machine.FluidOut
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.BlockSpec
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.wire.s2c.Aisle
import io.github.srdjanv.endreforked.wire.s2c.DropItemAction
import io.github.srdjanv.endreforked.wire.s2c.FluidAmount
import io.github.srdjanv.endreforked.wire.s2c.Ingredient
import io.github.srdjanv.endreforked.wire.s2c.ItemIngredient as WireItem
import io.github.srdjanv.endreforked.wire.s2c.ItemResult
import io.github.srdjanv.endreforked.wire.s2c.OreIngredient
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import io.github.srdjanv.endreforked.wire.s2c.SetBlockAction
import io.github.srdjanv.endreforked.wire.s2c.SymbolSpec
import io.github.srdjanv.endreforked.wire.s2c.WorldAction as WireAction
import io.github.srdjanv.endreforked.wire.s2c.BlockSpec as WireBlock
import io.github.srdjanv.endreforked.wire.s2c.StructureSpec as WireStructure
import io.github.srdjanv.endreforked.wire.s2c.WorldEvent as WireEvent
import io.github.srdjanv.endreforked.wire.s2c.MachineRecipe as WireRecipe
import io.github.srdjanv.endreforked.wire.s2c.MachineRecipes as WireRecipes

/** Mapping between machine recipes and their [RegistrySnapshot] message. */
fun MachineRecipes.toSnapshot() = RegistrySnapshot(
    registry = key.id.toString(),
    machine_recipes = WireRecipes(entries.map { it.toWire() }),
)

/** Returns null when [this] is not a machine recipe snapshot. Throws when an id in it is malformed. */
fun RegistrySnapshot.toMachineRecipes(): List<MachineRecipe>? = machine_recipes?.recipes?.map { it.toRecipe() }

private fun MachineRecipe.toWire() = WireRecipe(
    id = id.toString(),
    machine = machine.toString(),
    item_inputs = itemInputs.map {
        when (it) {
            is ItemIngredient.Item -> Ingredient(item = WireItem(it.id.toString(), it.meta, it.count))
            is ItemIngredient.OreDict -> Ingredient(ore = OreIngredient(it.name, it.count))
        }
    },
    fluid_inputs = fluidInputs.map { FluidAmount(it.id.toString(), it.amount) },
    item_outputs = itemOutputs.map { ItemResult(it.id.toString(), it.meta, it.count) },
    fluid_outputs = fluidOutputs.map { FluidAmount(it.id.toString(), it.amount) },
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
        is WorldAction.DropItem -> WireAction(drop_item = DropItemAction(ItemResult(a.item.id.toString(), a.item.meta, a.item.count)))
    },
)

private fun BlockSpec.toWire() = WireBlock(block.toString(), properties)

private fun WireRecipe.toRecipe() = MachineRecipe(
    id = Id.parse(id),
    machine = Id.parse(machine),
    itemInputs = item_inputs.map {
        val item = it.item
        val ore = it.ore
        when {
            item != null -> ItemIngredient.Item(Id.parse(item.id), item.meta, item.count)
            ore != null -> ItemIngredient.OreDict(ore.name, ore.count)
            else -> throw IllegalArgumentException("empty ingredient in recipe $id")
        }
    },
    fluidInputs = fluid_inputs.map { FluidIngredient(Id.parse(it.id), it.amount) },
    itemOutputs = item_outputs.map { ItemOut(Id.parse(it.id), it.meta, it.count) },
    fluidOutputs = fluid_outputs.map { FluidOut(Id.parse(it.id), it.amount) },
    ticks = ticks,
    entropy = entropy,
    priority = priority,    worldEvents = world_events.map { it.toEvent() },
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
                sym.symbol[0] to (sym.block ?: throw IllegalArgumentException("symbol without block")).toSpec()
            },
        ),
        action = when {
            set != null -> WorldAction.SetBlock((set.block ?: throw IllegalArgumentException("set_block without block")).toSpec())
            drop != null -> {
                val item = drop.item ?: throw IllegalArgumentException("drop_item without item")
                WorldAction.DropItem(ItemOut(Id.parse(item.id), item.meta, item.count))
            }
            else -> throw IllegalArgumentException("empty world action")
        },
    )
}

private fun WireBlock.toSpec() = BlockSpec(Id.parse(block), properties)
