package io.github.srdjanv.endreforked.api.world

import io.github.srdjanv.endreforked.api.machine.BlockSpec
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.api.registry.RegistryEntry

/*
 * The world interaction registries. Pure data, no behaviour, ids only.
 *
 * A [BlockSpec] used as a match is a block plus the properties that must have a value.
 * A property that is not named matches every value, so a spec without properties matches every state of the block.
 * A [BlockSpec] used as a result is the default state of the block with the named properties set.
 *
 * Of several entries that match, the highest `priority` is tried first, then the lowest id.
 */

/** A sound or other effect that server code plays after a change. */
enum class WorldEffect {
    /** The sound of a flint and steel. */
    FLINT_SOUND,
}

/**
 * What the Entropy Wand does to the block it is used on: it turns the block that fits [match] into [result].
 * The wand loses [itemDamage] durability, or the value of the server config when this is null.
 */
data class WandConversion(
    override val id: Id,
    val match: BlockSpec,
    val result: BlockSpec,
    val itemDamage: Int? = null,
    val priority: Int = 0,
    val effects: List<WorldEffect> = emptyList(),
) : RegistryEntry

/**
 * What a fluid block does to a neighbour block that fits [match]. It runs on random ticks of the fluid block and turns the neighbour into [result].
 * It happens in 1 of [chance] tries (1 means always).
 * With [consumeSource] it only happens at a source block, and afterwards the source is removed in 1 of [consumeChance] cases (0 means never).
 */
data class FluidBlockInteraction(
    override val id: Id,
    val fluid: Id,
    val match: BlockSpec,
    val result: BlockSpec,
    val chance: Int = 1,
    val consumeSource: Boolean = false,
    val consumeChance: Int = 0,
    val priority: Int = 0,
) : RegistryEntry

/** What happens when the entity of an interaction touches the fluid. */
sealed interface EntityResult {
    /** Spawns an item entity with [item] at the entity. */
    data class SpawnItem(val item: ItemOut) : EntityResult

    /** Spawns the entity [entity] at the entity. */
    data class SpawnEntity(val entity: Id) : EntityResult

    /** Replaces the fluid block with [block]. */
    data class SetFluidBlock(val block: BlockSpec) : EntityResult
}

/**
 * What a fluid block does to an entity of type [entity] that touches it. It runs on every collision tick.
 * An item entity (`minecraft:item`) needs an [item] that fits its stack, and loses [ItemIngredient.Item.count] of it.
 * Every other entity has no [item]. With [removeEntity] the entity is removed. Then [result] happens.
 * [chance], [consumeSource] and [consumeChance] work like in [FluidBlockInteraction].
 */
data class FluidEntityInteraction(
    override val id: Id,
    val fluid: Id,
    val entity: Id,
    val item: ItemIngredient.Item? = null,
    val removeEntity: Boolean = false,
    val result: EntityResult,
    val chance: Int = 1,
    val consumeSource: Boolean = false,
    val consumeChance: Int = 0,
    val priority: Int = 0,
) : RegistryEntry

/** The entity id of dropped items. [FluidEntityInteraction.item] is only used with it. */
val ITEM_ENTITY = Id("minecraft", "item")
