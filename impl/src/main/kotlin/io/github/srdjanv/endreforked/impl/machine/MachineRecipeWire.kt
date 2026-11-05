package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.FluidIngredient
import io.github.srdjanv.endreforked.api.machine.FluidOut
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.wire.s2c.FluidAmount
import io.github.srdjanv.endreforked.wire.s2c.Ingredient
import io.github.srdjanv.endreforked.wire.s2c.ItemIngredient as WireItem
import io.github.srdjanv.endreforked.wire.s2c.ItemResult
import io.github.srdjanv.endreforked.wire.s2c.OreIngredient
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
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
)

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
    priority = priority,
)
