package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.FluidIngredient
import io.github.srdjanv.endreforked.api.machine.FluidOut
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.WILDCARD_META
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.impl.registry.EntryCodec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * The JSON shape of a machine recipe. Unknown keys are an error, so typos are reported.
 *
 *     { "id": "endreborn:entropy_chamber/example", "machine": "endreborn:entropy_chamber", "ticks": 100, "entropy": 2,
 *       "itemInputs": [ { "item": "minecraft:end_stone", "meta": 0, "count": 4 }, { "ore": "logWood" } ],
 *       "fluidInputs": [ { "fluid": "minecraft:lava", "amount": 1000 } ],
 *       "itemOutputs": [ { "item": "minecraft:dirt", "count": 2 } ],
 *       "fluidOutputs": [ { "fluid": "minecraft:water", "amount": 1000 } ] }
 *
 * An input without "meta" matches every damage value. An output without "meta" is meta 0. "priority" is optional.
 */
object MachineRecipeJson : EntryCodec<MachineRecipe> {
    @Serializable
    private class Recipe(
        val id: String,
        val machine: String,
        val ticks: Int,
        val entropy: Int = 0,
        val priority: Int = 0,
        val itemInputs: List<ItemIn> = emptyList(),
        val fluidInputs: List<FluidStack> = emptyList(),
        val itemOutputs: List<ItemStack> = emptyList(),
        val fluidOutputs: List<FluidStack> = emptyList(),
    )

    @Serializable
    private class ItemIn(val item: String? = null, val ore: String? = null, val meta: Int = WILDCARD_META, val count: Int = 1)

    @Serializable
    private class ItemStack(val item: String, val meta: Int = 0, val count: Int = 1)

    @Serializable
    private class FluidStack(val fluid: String, val amount: Int)

    override fun decode(json: JsonObject): MachineRecipe {
        val r = Json.decodeFromJsonElement(Recipe.serializer(), json)
        return MachineRecipe(
            id = Id.parse(r.id),
            machine = Id.parse(r.machine),
            itemInputs = r.itemInputs.map {
                when {
                    it.item != null && it.ore == null -> ItemIngredient.Item(Id.parse(it.item), it.meta, it.count)
                    it.ore != null && it.item == null -> ItemIngredient.OreDict(it.ore, it.count)
                    else -> throw IllegalArgumentException("an item input needs exactly one of 'item' and 'ore'")
                }
            },
            fluidInputs = r.fluidInputs.map { FluidIngredient(Id.parse(it.fluid), it.amount) },
            itemOutputs = r.itemOutputs.map { ItemOut(Id.parse(it.item), it.meta, it.count) },
            fluidOutputs = r.fluidOutputs.map { FluidOut(Id.parse(it.fluid), it.amount) },
            ticks = r.ticks,
            entropy = r.entropy,
            priority = r.priority,
        )
    }
}
