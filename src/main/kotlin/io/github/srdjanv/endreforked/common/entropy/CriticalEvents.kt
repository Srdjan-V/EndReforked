package io.github.srdjanv.endreforked.common.entropy

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.api.entropy.CriticalAction
import io.github.srdjanv.endreforked.api.entropy.CriticalEvent
import net.minecraft.entity.EntityList
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import java.util.Random

/** Runs the critical events (see [EntropyWorld.fireCriticalEvents] for the trigger). */
object CriticalEvents {
    /** A player for whom these return true is anchored: blocks within [PROTECT_RADIUS] blocks of him never change. Filled by the Anchored trait. */
    val protectors: MutableList<(EntityPlayer) -> Boolean> = ArrayList()

    const val PROTECT_RADIUS = 8

    /** The event for [roll] in [0, total weight): events are taken in the order of the list. Null when there is none. */
    fun pick(events: List<CriticalEvent>, roll: Int): CriticalEvent? {
        var left = roll
        for (e in events) {
            if (left < e.weight) return e
            left -= e.weight
        }
        return null
    }

    fun pick(events: Collection<CriticalEvent>, rand: Random): CriticalEvent? {
        val list = events.toList()
        val total = list.sumOf { it.weight }
        return if (total <= 0) null else pick(list, rand.nextInt(total))
    }

    /** True when a player of [players] that is within [PROTECT_RADIUS] (by [distSq]) is anchored by one of [protectors]. */
    fun <P> isProtected(players: Iterable<P>, protectors: List<(P) -> Boolean>, distSq: (P) -> Double): Boolean =
        players.any { distSq(it) <= PROTECT_RADIUS * PROTECT_RADIUS && protectors.any { p -> p(it) } }

    private fun isProtected(world: WorldServer, pos: BlockPos): Boolean =
        protectors.isNotEmpty() && isProtected(world.playerEntities, protectors) { it.getDistanceSq(pos) }

    /** Runs [event] at [pos] (the first free block above the surface). */
    fun run(world: WorldServer, event: CriticalEvent, pos: BlockPos) {
        EndReforked.LOGGER.debug("Critical event {} at {}", event.id, pos)
        when (val a = event.action) {
            is CriticalAction.Convert -> convert(world, pos.down(), event.radius)
            is CriticalAction.SpawnMobs -> spawn(world, pos, a)
            is CriticalAction.Cloud -> DragonBreathCloud.spawn(world, pos, a.radius, a.durationTicks, effect = a.effect)
        }
    }

    private fun convert(world: WorldServer, center: BlockPos, radius: Int) {
        val conversions = EndReforked.getProxy().registries(world).corruptionConversions.current
        val candidates = BlockPos.getAllInBox(center.add(-radius, -radius, -radius), center.add(radius, radius, radius))
            .map { it.toImmutable() }
            .filter { it.distanceSq(center) <= radius * radius && world.isBlockLoaded(it) }
            .shuffled(world.rand)
        var left = maxOf(1, radius * 2)
        for (p in candidates) {
            if (left == 0) break
            val state = world.getBlockState(p)
            val conversion = conversions.forBlock(state.block).firstOrNull { it.match.matches(state) } ?: continue
            if (state.block.hasTileEntity(state) || world.getTileEntity(p) != null || isProtected(world, p)) continue
            world.setBlockState(p, conversion.result)
            left--
        }
    }

    private fun spawn(world: WorldServer, pos: BlockPos, a: CriticalAction.SpawnMobs) {
        repeat(a.min + world.rand.nextInt(a.max - a.min + 1)) {
            val entity = EntityList.createEntityByIDFromName(a.entities[world.rand.nextInt(a.entities.size)], world) ?: return@repeat
            for (try_ in 1..5) { // five tries to find free ground near the position
                val x = pos.x + world.rand.nextInt(7) - 3
                val z = pos.z + world.rand.nextInt(7) - 3
                if (!world.isBlockLoaded(BlockPos(x, 0, z))) continue
                val y = world.getHeight(x, z)
                entity.setLocationAndAngles(x + 0.5, y.toDouble(), z + 0.5, world.rand.nextFloat() * 360f, 0f)
                if (y > 0 && (entity !is EntityLiving || entity.isNotColliding)) {
                    (entity as? EntityLiving)?.onInitialSpawn(world.getDifficultyForLocation(pos), null)
                    world.spawnEntity(entity)
                    break
                }
            }
        }
    }
}
