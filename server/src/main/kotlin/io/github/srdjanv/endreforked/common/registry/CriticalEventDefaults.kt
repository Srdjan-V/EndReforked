package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.entropy.CriticalAction
import io.github.srdjanv.endreforked.api.entropy.CriticalEvent
import net.minecraft.init.MobEffects
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation

/** The critical events that ship with the mod. */
object CriticalEventDefaults {
    private fun id(name: String) = ResourceLocation(Tags.MODID, "critical/$name")

    fun all() = listOf(
        CriticalEvent(id("corruption"), 10, 3, 20, CriticalAction.Convert),
        CriticalEvent(
            id("rift"), 5, 0, 60,
            CriticalAction.SpawnMobs(listOf(ResourceLocation(Tags.MODID, "watcher"), ResourceLocation(Tags.MODID, "endguard")), 1, 3),
        ),
        // like the dragon breath of the vanilla dragon
        CriticalEvent(id("burst"), 2, 4, 150, CriticalAction.Cloud(4f, 200, PotionEffect(MobEffects.INSTANT_DAMAGE))),
    )
}
