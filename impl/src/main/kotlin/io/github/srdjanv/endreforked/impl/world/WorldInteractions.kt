package io.github.srdjanv.endreforked.impl.world

import io.github.srdjanv.endreforked.api.world.EntityResult
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.ITEM_ENTITY
import io.github.srdjanv.endreforked.api.world.WandConversion
import io.github.srdjanv.endreforked.impl.registry.FrozenRegistry
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import net.minecraft.block.Block
import net.minecraft.entity.EntityList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid

/** The conversions of the Entropy Wand, indexed by the block they match. */
class WandConversions(entries: Collection<WandConversion>) : FrozenRegistry<WandConversion>(KEY, entries) {
    private val byBlock: Map<Block, List<WandConversion>> =
        this.entries.sortedWith(ORDER).groupBy { it.match.block }

    /** The conversions for [block], first to try first. */
    fun forBlock(block: Block): List<WandConversion> = byBlock[block] ?: emptyList()

    companion object {
        val KEY = ResourceLocation("endreborn", "wand_conversions")

        private val ORDER = compareByDescending<WandConversion> { it.priority }.thenBy { it.id }

        fun managed(defaults: () -> Collection<WandConversion>) = ManagedRegistry(KEY, defaults, ::validate, ::WandConversions)

        fun validate(c: WandConversion): String? = when {
            c.itemDamage != null && c.itemDamage!! < 0 -> "itemDamage must not be negative"
            else -> null
        }
    }
}

/** The reactions of fluid blocks with the blocks next to them, indexed by fluid and neighbour block. */
class FluidBlockInteractions(entries: Collection<FluidBlockInteraction>) :
    FrozenRegistry<FluidBlockInteraction>(KEY, entries) {
    private val index: Map<Fluid, Map<Block, List<FluidBlockInteraction>>> =
        this.entries.sortedWith(ORDER).groupBy({ it.fluid }).mapValues { (_, v) -> v.groupBy { it.match.block } }

    /** The interactions of [fluid] with [block], first to try first. */
    fun find(fluid: Fluid, block: Block): List<FluidBlockInteraction> = index[fluid]?.get(block) ?: emptyList()

    companion object {
        val KEY = ResourceLocation("endreborn", "fluid_block_interactions")

        private val ORDER = compareByDescending<FluidBlockInteraction> { it.priority }.thenBy { it.id }

        fun managed(defaults: () -> Collection<FluidBlockInteraction>) = ManagedRegistry(KEY, defaults, ::validate, ::FluidBlockInteractions)

        fun validate(i: FluidBlockInteraction): String? = reactionProblem(i.chance, i.consumeSource, i.consumeChance)
    }
}

/** The reactions of fluid blocks with entities, indexed by fluid and entity type. */
class FluidEntityInteractions(entries: Collection<FluidEntityInteraction>) :
    FrozenRegistry<FluidEntityInteraction>(KEY, entries) {
    private val index: Map<Fluid, Map<ResourceLocation, List<FluidEntityInteraction>>> =
        this.entries.sortedWith(ORDER).groupBy({ it.fluid }).mapValues { (_, v) -> v.groupBy { it.entity } }

    /** The interactions of [fluid] with entities of type [entity], first to try first. */
    fun find(fluid: Fluid, entity: ResourceLocation): List<FluidEntityInteraction> = index[fluid]?.get(entity) ?: emptyList()

    companion object {
        val KEY = ResourceLocation("endreborn", "fluid_entity_interactions")

        private val ORDER = compareByDescending<FluidEntityInteraction> { it.priority }.thenBy { it.id }

        fun managed(defaults: () -> Collection<FluidEntityInteraction>) = ManagedRegistry(KEY, defaults, ::validate, ::FluidEntityInteractions)

        // The entity types are registered when the registries are built (init), also on the client.
        private fun entityProblem(entity: ResourceLocation) = if (EntityList.getClass(entity) == null) "unknown entity $entity" else null

        fun validate(i: FluidEntityInteraction): String? {
            reactionProblem(i.chance, i.consumeSource, i.consumeChance)?.let { return it }
            entityProblem(i.entity)?.let { return it }
            val item = i.item
            if (i.entity == ITEM_ENTITY && item == null) return "an interaction with $ITEM_ENTITY needs an item"
            if (i.entity != ITEM_ENTITY && item != null) return "only an interaction with $ITEM_ENTITY has an item"
            if (item != null && item.isEmpty) return "item is empty"
            return when (val r = i.result) {
                is EntityResult.SpawnItem -> if (r.item.isEmpty) "result item is empty" else null
                is EntityResult.SpawnEntity -> entityProblem(r.entity)
                is EntityResult.SetFluidBlock -> null
            }
        }
    }
}

private fun reactionProblem(chance: Int, consumeSource: Boolean, consumeChance: Int): String? = when {
    chance < 1 -> "chance must be at least 1"
    consumeChance < 0 -> "consumeChance must not be negative"
    consumeChance > 0 && !consumeSource -> "consumeChance needs consumeSource"
    else -> null
}
