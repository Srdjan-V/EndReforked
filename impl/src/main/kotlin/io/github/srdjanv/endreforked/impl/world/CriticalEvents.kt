package io.github.srdjanv.endreforked.impl.world

import io.github.srdjanv.endreforked.api.entropy.CriticalAction
import io.github.srdjanv.endreforked.api.entropy.CriticalEvent
import io.github.srdjanv.endreforked.impl.registry.FrozenRegistry
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import net.minecraft.entity.EntityList
import net.minecraft.util.ResourceLocation

/** The critical events of the chunks. No client has this registry. */
class CriticalEvents(entries: Collection<CriticalEvent>) : FrozenRegistry<CriticalEvent>(KEY, entries) {
    companion object {
        val KEY = ResourceLocation("endreborn", "critical_events")

        fun managed(defaults: () -> Collection<CriticalEvent>) = ManagedRegistry(KEY, defaults, ::validate, ::CriticalEvents)

        fun validate(e: CriticalEvent): String? {
            if (e.weight < 1) return "weight must be at least 1"
            if (e.radius < 0 || e.entropyCost < 0) return "radius and entropyCost must not be negative"
            return when (val a = e.action) {
                is CriticalAction.Convert -> null
                is CriticalAction.SpawnMobs ->
                    if (a.entities.isEmpty() || a.min < 0 || a.max < a.min) "needs entities and 0 <= min <= max"
                    else a.entities.firstOrNull { EntityList.getClass(it) == null }?.let { "unknown entity $it" }
                is CriticalAction.Cloud -> if (a.radius <= 0f || a.durationTicks < 1) "needs radius > 0 and duration >= 1" else null
            }
        }
    }
}
