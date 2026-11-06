package io.github.srdjanv.endreforked.api.entropy

import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation

/** What a critical event does. Block changes are skipped for blocks with a tile entity and near an anchored player. */
sealed interface CriticalAction {
    /** Converts up to `2 * radius` blocks in the radius with the wand conversions (slow corruption). */
    data object Convert : CriticalAction

    /** Spawns [min] to [max] mobs, each of a random type of [entities], on the ground near the position. */
    data class SpawnMobs(val entities: List<ResourceLocation>, val min: Int, val max: Int) : CriticalAction

    /** A lingering dragon breath cloud of [radius] blocks that lasts [durationTicks] and applies [effect]. */
    data class Cloud(val radius: Float, val durationTicks: Int, val effect: PotionEffect) : CriticalAction
}

/**
 * An effect at a random position in a critical chunk. A chunk rolls one event by [weight] and pays [entropyCost].
 * [radius] is in blocks. Server only.
 */
data class CriticalEvent(
    override val id: ResourceLocation,
    val weight: Int,
    val radius: Int,
    val entropyCost: Int,
    val action: CriticalAction,
) : RegistryEntry
