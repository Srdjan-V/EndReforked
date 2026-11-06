package io.github.srdjanv.endreforked.impl.world

import io.github.srdjanv.endreforked.api.machine.WILDCARD_META
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.api.registry.RegistryKey
import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.ITEM_ENTITY
import io.github.srdjanv.endreforked.api.world.WandConversion
import io.github.srdjanv.endreforked.impl.registry.FrozenRegistry
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry

/** The conversions of the Entropy Wand, indexed by the block they match. */
class WandConversions(entries: Collection<WandConversion>) : FrozenRegistry<WandConversion>(KEY, entries) {
    private val byBlock: Map<Id, List<WandConversion>> =
        this.entries.sortedWith(ORDER).groupBy { it.match.block }

    /** The conversions for [block], first to try first. */
    fun forBlock(block: Id): List<WandConversion> = byBlock[block] ?: emptyList()

    companion object {
        val KEY = RegistryKey<WandConversion>(Id("endreborn", "wand_conversions"))

        private val ORDER = compareByDescending<WandConversion> { it.priority }.thenBy { it.id }

        fun managed(defaults: () -> Collection<WandConversion>, extraValidation: (WandConversion) -> String? = { null }) =
            ManagedRegistry(KEY, defaults, { validate(it) ?: extraValidation(it) }, ::WandConversions)

        fun validate(c: WandConversion): String? = when {
            c.itemDamage != null && c.itemDamage!! < 0 -> "itemDamage must not be negative"
            else -> null
        }
    }
}

/** The reactions of fluid blocks with the blocks next to them, indexed by fluid and neighbour block. */
class FluidBlockInteractions(entries: Collection<FluidBlockInteraction>) :
    FrozenRegistry<FluidBlockInteraction>(KEY, entries) {
    private val index: Map<Id, Map<Id, List<FluidBlockInteraction>>> =
        this.entries.sortedWith(ORDER).groupBy({ it.fluid }).mapValues { (_, v) -> v.groupBy { it.match.block } }

    /** The interactions of [fluid] with [block], first to try first. */
    fun find(fluid: Id, block: Id): List<FluidBlockInteraction> = index[fluid]?.get(block) ?: emptyList()

    companion object {
        val KEY = RegistryKey<FluidBlockInteraction>(Id("endreborn", "fluid_block_interactions"))

        private val ORDER = compareByDescending<FluidBlockInteraction> { it.priority }.thenBy { it.id }

        fun managed(defaults: () -> Collection<FluidBlockInteraction>, extraValidation: (FluidBlockInteraction) -> String? = { null }) =
            ManagedRegistry(KEY, defaults, { validate(it) ?: extraValidation(it) }, ::FluidBlockInteractions)

        fun validate(i: FluidBlockInteraction): String? = reactionProblem(i.chance, i.consumeSource, i.consumeChance)
    }
}

/** The reactions of fluid blocks with entities, indexed by fluid and entity type. */
class FluidEntityInteractions(entries: Collection<FluidEntityInteraction>) :
    FrozenRegistry<FluidEntityInteraction>(KEY, entries) {
    private val index: Map<Id, Map<Id, List<FluidEntityInteraction>>> =
        this.entries.sortedWith(ORDER).groupBy({ it.fluid }).mapValues { (_, v) -> v.groupBy { it.entity } }

    /** The interactions of [fluid] with entities of type [entity], first to try first. */
    fun find(fluid: Id, entity: Id): List<FluidEntityInteraction> = index[fluid]?.get(entity) ?: emptyList()

    companion object {
        val KEY = RegistryKey<FluidEntityInteraction>(Id("endreborn", "fluid_entity_interactions"))

        private val ORDER = compareByDescending<FluidEntityInteraction> { it.priority }.thenBy { it.id }

        fun managed(defaults: () -> Collection<FluidEntityInteraction>, extraValidation: (FluidEntityInteraction) -> String? = { null }) =
            ManagedRegistry(KEY, defaults, { validate(it) ?: extraValidation(it) }, ::FluidEntityInteractions)

        fun validate(i: FluidEntityInteraction): String? {
            reactionProblem(i.chance, i.consumeSource, i.consumeChance)?.let { return it }
            val item = i.item
            if (i.entity == ITEM_ENTITY && item == null) return "an interaction with ${ITEM_ENTITY} needs an item"
            if (i.entity != ITEM_ENTITY && item != null) return "only an interaction with ${ITEM_ENTITY} has an item"
            if (item != null && (item.count < 1 || item.meta !in 0..WILDCARD_META)) return "item count or meta is invalid"
            val r = i.result
            return if (r is EntityResult.SpawnItem && (r.item.count < 1 || r.item.meta !in 0..WILDCARD_META)) "result item count or meta is invalid" else null
        }
    }
}

private fun reactionProblem(chance: Int, consumeSource: Boolean, consumeChance: Int): String? = when {
    chance < 1 -> "chance must be at least 1"
    consumeChance < 0 -> "consumeChance must not be negative"
    consumeChance > 0 && !consumeSource -> "consumeChance needs consumeSource"
    else -> null
}
