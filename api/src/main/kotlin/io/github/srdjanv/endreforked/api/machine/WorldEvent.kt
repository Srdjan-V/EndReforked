package io.github.srdjanv.endreforked.api.machine

import io.github.srdjanv.endreforked.api.registry.Id

/** A block with some properties set, for example `minecraft:stone` with `variant=granite`. Properties that are not named keep the default of the block. */
data class BlockSpec(val block: Id, val properties: Map<String, String> = emptyMap())

/**
 * A block pattern. `aisles[a][r]` is one row, and the characters of a row run along x. [symbols] maps each character to the block there.
 * The blank [ANY] matches every block, needs no entry in [symbols], and shows as air in a preview.
 * A position matches only when its block state equals the state of its [BlockSpec] exactly.
 * The pattern matches in every orientation.
 */
data class StructureSpec(val aisles: List<List<String>>, val symbols: Map<Char, BlockSpec>) {
    companion object {
        const val ANY = ' '
    }
}

/** What a world event does to each matched block that is not [StructureSpec.ANY] and not air. */
sealed interface WorldAction {
    /** Replaces the block with [block]. */
    data class SetBlock(val block: BlockSpec) : WorldAction

    /** Removes the block and drops [item] in its place. */
    data class DropItem(val item: ItemOut) : WorldAction
}

/**
 * Something that can happen to the world while a machine runs a recipe (the Materializer).
 * At [atPercent] (1 to 100) of the recipe progress, it triggers with [chance] percent.
 * Then it needs a match of [structure] in the area around the machine, and runs [action] on it.
 * When it triggered but found no match, the machine run fails.
 * A recipe has at most one event per percent.
 */
data class WorldEvent(val atPercent: Int, val chance: Int, val structure: StructureSpec, val action: WorldAction)
