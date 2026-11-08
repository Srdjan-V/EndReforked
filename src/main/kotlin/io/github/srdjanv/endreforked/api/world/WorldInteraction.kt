package io.github.srdjanv.endreforked.api.world

import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid

/*
 * The world interaction registries. Pure data, no behaviour, Minecraft objects.
 * A result is a block state. A match is a [BlockMatch].
 * Of several entries that match, the highest `priority` is tried first, then the lowest id.
 */

/**
 * A block plus the properties that must have a value. A property that is not in [properties] matches every value,
 * so a match without properties matches every state of the block.
 */
data class BlockMatch(val block: Block, val properties: Map<IProperty<*>, Comparable<*>> = emptyMap()) {
    constructor(state: IBlockState) : this(state.block, state.properties)

    /** True when [state] is of [block] and has the value of every property in [properties]. */
    fun matches(state: IBlockState) = state.block === block && properties.all { (p, v) -> state.properties[p] == v }

    /** The default state of [block] with [properties] set. */
    @Suppress("UNCHECKED_CAST")
    fun toState(): IBlockState = properties.entries.fold(block.defaultState) { state, (p, v) ->
        state.withProperty(p as IProperty<Comparable<Any>>, v as Comparable<Any>)
    }
}

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
    override val id: ResourceLocation,
    val match: BlockMatch,
    val result: IBlockState,
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
    override val id: ResourceLocation,
    val fluid: Fluid,
    val match: BlockMatch,
    val result: IBlockState,
    val chance: Int = 1,
    val consumeSource: Boolean = false,
    val consumeChance: Int = 0,
    val priority: Int = 0,
) : RegistryEntry

/** What happens when the entity of an interaction touches the fluid. */
sealed interface EntityResult {
    /** Spawns an item entity with [item] at the entity. */
    data class SpawnItem(val item: ItemStack) : EntityResult

    /** Spawns the entity [entity] at the entity. */
    data class SpawnEntity(val entity: ResourceLocation) : EntityResult

    /** Replaces the fluid block with [block]. */
    data class SetFluidBlock(val block: IBlockState) : EntityResult
}

/**
 * What a fluid block does to an entity of type [entity] that touches it. It runs on every collision tick.
 * An item entity (`minecraft:item`) needs an [item] that fits its stack, and loses the count of [item] from it. The meta of [item] is exact or `OreDictionary.WILDCARD_VALUE`.
 * Every other entity has no [item]. With [removeEntity] the entity is removed. Then [result] happens.
 * [chance], [consumeSource] and [consumeChance] work like in [FluidBlockInteraction].
 */
data class FluidEntityInteraction(
    override val id: ResourceLocation,
    val fluid: Fluid,
    val entity: ResourceLocation,
    val item: ItemStack? = null,
    val removeEntity: Boolean = false,
    val result: EntityResult,
    val chance: Int = 1,
    val consumeSource: Boolean = false,
    val consumeChance: Int = 0,
    val priority: Int = 0,
) : RegistryEntry

/** The entity id of dropped items. [FluidEntityInteraction.item] is only used with it. */
val ITEM_ENTITY = ResourceLocation("minecraft", "item")
