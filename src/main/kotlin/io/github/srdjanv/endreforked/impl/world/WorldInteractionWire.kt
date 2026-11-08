package io.github.srdjanv.endreforked.impl.world

import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.WandConversion
import io.github.srdjanv.endreforked.api.world.WorldEffect
import io.github.srdjanv.endreforked.impl.registry.decodeEntries
import io.github.srdjanv.endreforked.impl.registry.fluidOf
import io.github.srdjanv.endreforked.impl.registry.toItemStack
import io.github.srdjanv.endreforked.impl.registry.toMatch
import io.github.srdjanv.endreforked.impl.registry.toState
import io.github.srdjanv.endreforked.impl.registry.toWire
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import io.github.srdjanv.endreforked.wire.s2c.EntityResult as WireResult
import io.github.srdjanv.endreforked.wire.s2c.FluidBlockInteraction as WireBlockInteraction
import io.github.srdjanv.endreforked.wire.s2c.FluidBlockInteractions as WireBlockInteractions
import io.github.srdjanv.endreforked.wire.s2c.FluidEntityInteraction as WireEntityInteraction
import io.github.srdjanv.endreforked.wire.s2c.FluidEntityInteractions as WireEntityInteractions
import io.github.srdjanv.endreforked.wire.s2c.WandConversion as WireConversion
import io.github.srdjanv.endreforked.wire.s2c.WandConversions as WireConversions
import io.github.srdjanv.endreforked.wire.s2c.WorldEffect as WireEffect
import net.minecraft.util.ResourceLocation

// Mapping between the world interaction registries and their RegistrySnapshot messages. Each `to...` list function returns null for a snapshot of another registry.

fun WandConversions.toSnapshot() = RegistrySnapshot(registry = key.toString(), wand_conversions = WireConversions(entries.map { it.toWire() }))

fun FluidBlockInteractions.toSnapshot() =
    RegistrySnapshot(registry = key.toString(), fluid_block_interactions = WireBlockInteractions(entries.map { it.toWire() }))

fun FluidEntityInteractions.toSnapshot() =
    RegistrySnapshot(registry = key.toString(), fluid_entity_interactions = WireEntityInteractions(entries.map { it.toWire() }))

/** An entry with something this client does not have is skipped. */
fun RegistrySnapshot.toWandConversions(): List<WandConversion>? = wand_conversions?.conversions?.decodeEntries({ it.id }) { it.toEntry() }

fun RegistrySnapshot.toFluidBlockInteractions(): List<FluidBlockInteraction>? = fluid_block_interactions?.interactions?.decodeEntries({ it.id }) { it.toEntry() }

fun RegistrySnapshot.toFluidEntityInteractions(): List<FluidEntityInteraction>? = fluid_entity_interactions?.interactions?.decodeEntries({ it.id }) { it.toEntry() }

private fun WandConversion.toWire() = WireConversion(
    id = id.toString(),
    match = match.toWire(),
    result = result.toWire(),
    item_damage = itemDamage,
    priority = priority,
    effects = effects.map { WireEffect.valueOf(it.name) },
)

private fun WireConversion.toEntry() = WandConversion(
    id = ResourceLocation(id),
    match = (match ?: throw IllegalArgumentException("conversion $id without match")).toMatch(),
    result = (result ?: throw IllegalArgumentException("conversion $id without result")).toState(),
    itemDamage = item_damage,
    priority = priority,
    effects = effects.filter { it != WireEffect.WORLD_EFFECT_UNKNOWN }.map { WorldEffect.valueOf(it.name) },
)

private fun FluidBlockInteraction.toWire() = WireBlockInteraction(
    id.toString(), fluid.toWire(), match.toWire(), result.toWire(), chance, consumeSource, consumeChance, priority,
)

private fun WireBlockInteraction.toEntry() = FluidBlockInteraction(
    id = ResourceLocation(id),
    fluid = fluidOf(fluid),
    match = (match ?: throw IllegalArgumentException("interaction $id without match")).toMatch(),
    result = (result ?: throw IllegalArgumentException("interaction $id without result")).toState(),
    chance = chance,
    consumeSource = consume_source,
    consumeChance = consume_chance,
    priority = priority,
)

private fun FluidEntityInteraction.toWire() = WireEntityInteraction(
    id = id.toString(),
    fluid = fluid.toWire(),
    entity = entity.toString(),
    item = item?.toWire(),
    remove_entity = removeEntity,
    result = when (val r = result) {
        is EntityResult.SpawnItem -> WireResult(spawn_item = r.item.toWire())
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
        id = ResourceLocation(id),
        fluid = fluidOf(fluid),
        entity = ResourceLocation(entity),
        item = item?.toItemStack(),
        removeEntity = remove_entity,
        result = when {
            spawnItem != null -> EntityResult.SpawnItem(spawnItem.toItemStack())
            spawnEntity != null -> EntityResult.SpawnEntity(ResourceLocation(spawnEntity))
            setBlock != null -> EntityResult.SetFluidBlock(setBlock.toState())
            else -> throw IllegalArgumentException("empty result in interaction $id")
        },
        chance = chance,
        consumeSource = consume_source,
        consumeChance = consume_chance,
        priority = priority,
    )
}
