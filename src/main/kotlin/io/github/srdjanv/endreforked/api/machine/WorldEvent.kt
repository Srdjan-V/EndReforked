package io.github.srdjanv.endreforked.api.machine

import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack

/**
 * A block pattern. `aisles[a][r]` is one row, and the characters of a row run along x. [symbols] maps each character to the block there.
 * The blank [ANY] matches every block, needs no entry in [symbols], and shows as air in a preview.
 * A position matches only when its block state equals the state of its symbol exactly.
 * The pattern matches in every orientation.
 */
data class StructureSpec(val aisles: List<List<String>>, val symbols: Map<Char, IBlockState>) {
    companion object {
        const val ANY = ' '
    }
}

/** What a world event does to each matched block that is not [StructureSpec.ANY] and not air. */
sealed interface WorldAction {
    /** Replaces the block with [block]. */
    data class SetBlock(val block: IBlockState) : WorldAction

    /** Removes the block and drops [item] in its place. */
    data class DropItem(val item: ItemStack) : WorldAction
}

/**
 * Something that can happen to the world while a machine runs a recipe (the Materializer).
 * At [atPercent] (1 to 100) of the recipe progress, it triggers with [chance] percent.
 * Then it needs a match of [structure] in the area around the machine, and runs [action] on it.
 * When it triggered but found no match, the machine run fails.
 * A recipe has at most one event per percent.
 */
data class WorldEvent(val atPercent: Int, val chance: Int, val structure: StructureSpec, val action: WorldAction)
