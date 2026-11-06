package io.github.srdjanv.endreforked.impl.world

import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.WandConversion
import io.github.srdjanv.endreforked.api.world.WorldEffect
import io.github.srdjanv.endreforked.impl.machine.toSpec
import io.github.srdjanv.endreforked.impl.machine.toWire
import io.github.srdjanv.endreforked.wire.s2c.ItemIngredient as WireItem
import io.github.srdjanv.endreforked.wire.s2c.ItemResult
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import io.github.srdjanv.endreforked.wire.s2c.EntityResult as WireResult
import io.github.srdjanv.endreforked.wire.s2c.FluidBlockInteraction as WireBlockInteraction
import io.github.srdjanv.endreforked.wire.s2c.FluidBlockInteractions as WireBlockInteractions
import io.github.srdjanv.endreforked.wire.s2c.FluidEntityInteraction as WireEntityInteraction
import io.github.srdjanv.endreforked.wire.s2c.FluidEntityInteractions as WireEntityInteractions
import io.github.srdjanv.endreforked.wire.s2c.WandConversion as WireConversion
import io.github.srdjanv.endreforked.wire.s2c.WandConversions as WireConversions
import io.github.srdjanv.endreforked.wire.s2c.WorldEffect as WireEffect

// Mapping between the world interaction registries and their RegistrySnapshot messages. Each `to...` list function returns null for a snapshot of another registry.

fun WandConversions.toSnapshot() = RegistrySnapshot(registry = key.id.toString(), wand_conversions = WireConversions(entries.map { it.toWire() }))

fun FluidBlockInteractions.toSnapshot() =
    RegistrySnapshot(registry = key.id.toString(), fluid_block_interactions = WireBlockInteractions(entries.map { it.toWire() }))

fun FluidEntityInteractions.toSnapshot() =
    RegistrySnapshot(registry = key.id.toString(), fluid_entity_interactions = WireEntityInteractions(entries.map { it.toWire() }))

/** Throws when an id in the snapshot is malformed. */
fun RegistrySnapshot.toWandConversions(): List<WandConversion>? = wand_conversions?.conversions?.map { it.toEntry() }

fun RegistrySnapshot.toFluidBlockInteractions(): List<FluidBlockInteraction>? = fluid_block_interactions?.interactions?.map { it.toEntry() }

fun RegistrySnapshot.toFluidEntityInteractions(): List<FluidEntityInteraction>? = fluid_entity_interactions?.interactions?.map { it.toEntry() }

private fun WandConversion.toWire() = WireConversion(
    id = id.toString(),
    match = match.toWire(),
    result = result.toWire(),
    item_damage = itemDamage,
    priority = priority,
    effects = effects.map { WireEffect.valueOf(it.name) },
)

private fun WireConversion.toEntry() = WandConversion(
    id = Id.parse(id),
    match = (match ?: throw IllegalArgumentException("conversion $id without match")).toSpec(),
    result = (result ?: throw IllegalArgumentException("conversion $id without result")).toSpec(),
    itemDamage = item_damage,
    priority = priority,
    effects = effects.filter { it != WireEffect.WORLD_EFFECT_UNKNOWN }.map { WorldEffect.valueOf(it.name) },
)

private fun FluidBlockInteraction.toWire() = WireBlockInteraction(
    id.toString(), fluid.toString(), match.toWire(), result.toWire(), chance, consumeSource, consumeChance, priority,
)

private fun WireBlockInteraction.toEntry() = FluidBlockInteraction(
    id = Id.parse(id),
    fluid = Id.parse(fluid),
    match = (match ?: throw IllegalArgumentException("interaction $id without match")).toSpec(),
    result = (result ?: throw IllegalArgumentException("interaction $id without result")).toSpec(),
    chance = chance,
    consumeSource = consume_source,
    consumeChance = consume_chance,
    priority = priority,
)

private fun FluidEntityInteraction.toWire() = WireEntityInteraction(
    id = id.toString(),
    fluid = fluid.toString(),
    entity = entity.toString(),
    item = item?.let { WireItem(it.id.toString(), it.meta, it.count) },
    remove_entity = removeEntity,
    result = when (val r = result) {
        is EntityResult.SpawnItem -> WireResult(spawn_item = ItemResult(r.item.id.toString(), r.item.meta, r.item.count))
        is EntityResult.SpawnEntity -> WireResult(spawn_entity = r.entity.toString())
        is EntityResult.SetFluidBlock -> WireResult(set_fluid_block = r.block.toWire())
    },
    chance = chance,
    consume_source = consumeSource,
    consume_chance = consumeChance,
    priority = priority,
)

private fun WireEntityInteraction.toEntry(): FluidEntityInteraction {
    val r = result ?: throw IllegalArgumentException("interaction $id without result")
    val spawnItem = r.spawn_item
    val spawnEntity = r.spawn_entity
    val setBlock = r.set_fluid_block
    return FluidEntityInteraction(
        id = Id.parse(id),
        fluid = Id.parse(fluid),
        entity = Id.parse(entity),
        item = item?.let { ItemIngredient.Item(Id.parse(it.id), it.meta, it.count) },
        removeEntity = remove_entity,
        result = when {
            spawnItem != null -> EntityResult.SpawnItem(ItemOut(Id.parse(spawnItem.id), spawnItem.meta, spawnItem.count))
            spawnEntity != null -> EntityResult.SpawnEntity(Id.parse(spawnEntity))
            setBlock != null -> EntityResult.SetFluidBlock(setBlock.toSpec())
            else -> throw IllegalArgumentException("empty result in interaction $id")
        },
        chance = chance,
        consumeSource = consume_source,
        consumeChance = consume_chance,
        priority = priority,
    )
}
