package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.BlockSpec
import io.github.srdjanv.endreforked.api.machine.FluidIngredient
import io.github.srdjanv.endreforked.api.machine.FluidOut
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WILDCARD_META
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
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
 *
 * "worldEvents" is optional and only the Materializer uses it. "aisles" is a list of aisles, each a list of rows.
 * A blank in a row matches any block. Property values are strings. The action "type" is "set_block" or "drop_item".
 *
 *     "worldEvents": [ { "atPercent": 30, "chance": 15,
 *       "structure": { "aisles": [ ["SS"] ], "symbols": { "S": { "block": "minecraft:stone", "properties": {} } } },
 *       "action": { "type": "set_block", "block": "minecraft:iron_block" } },
 *       { "atPercent": 90, "chance": 2, "structure": { ... },
 *         "action": { "type": "drop_item", "item": "minecraft:diamond", "meta": 0, "count": 1 } } ]
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
        val worldEvents: List<Event> = emptyList(),
    )

    @Serializable
    private class Event(val atPercent: Int, val chance: Int, val structure: Structure, val action: Action)

    @Serializable
    private class Structure(val aisles: List<List<String>>, val symbols: Map<String, Block>)

    @Serializable
    private class Block(val block: String, val properties: Map<String, String> = emptyMap())

    @Serializable
    private class Action(
        val type: String,
        val block: String? = null,
        val properties: Map<String, String> = emptyMap(),
        val item: String? = null,
        val meta: Int = 0,
        val count: Int = 1,
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
            worldEvents = r.worldEvents.map(::event),
        )
    }

    private fun event(e: Event) = WorldEvent(
        atPercent = e.atPercent,
        chance = e.chance,
        structure = StructureSpec(
            e.structure.aisles,
            e.structure.symbols.entries.associate { (k, v) ->
                require(k.length == 1) { "symbol '$k' is not one character" }
                k[0] to BlockSpec(Id.parse(v.block), v.properties)
            },
        ),
        action = e.action.let { a ->
            when (a.type) {
                "set_block" -> {
                    require(a.item == null) { "set_block has no 'item'" }
                    WorldAction.SetBlock(BlockSpec(Id.parse(a.block ?: throw IllegalArgumentException("set_block needs 'block'")), a.properties))
                }
                "drop_item" -> {
                    require(a.block == null && a.properties.isEmpty()) { "drop_item has no 'block' or 'properties'" }
                    WorldAction.DropItem(ItemOut(Id.parse(a.item ?: throw IllegalArgumentException("drop_item needs 'item'")), a.meta, a.count))
                }
                else -> throw IllegalArgumentException("unknown action type '${a.type}'")
            }
        },
    )
}
