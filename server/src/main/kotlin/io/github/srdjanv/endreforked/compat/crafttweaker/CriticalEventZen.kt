package io.github.srdjanv.endreforked.compat.crafttweaker

import crafttweaker.annotations.ZenRegister
import io.github.srdjanv.endreforked.api.entropy.CriticalAction
import io.github.srdjanv.endreforked.api.entropy.CriticalEvent
import net.minecraft.init.MobEffects
import net.minecraft.potion.PotionEffect
import stanhebben.zenscript.annotations.ZenClass
import stanhebben.zenscript.annotations.ZenMethod

/** `mods.endreborn.CriticalEvents`: what happens in a critical chunk. The id is `namespace:path`. */
@ZenRegister
@ZenClass("mods.endreborn.CriticalEvents")
object CriticalEventZen {
    private val layer get() = CtLayers.criticalEvents

    private fun add(id: String, weight: Int, radius: Int, cost: Int, action: CriticalAction) =
        act("Adding the End Reforked critical event $id") { layer.add(layer.parse(id), "critical") { CriticalEvent(it, weight, radius, cost, action) } }

    /** Converts blocks in the radius with the wand conversions. */
    @JvmStatic @ZenMethod fun addConvert(id: String, weight: Int, radius: Int, cost: Int) = add(id, weight, radius, cost, CriticalAction.Convert)

    /** Spawns [min] to [max] mobs of these entity ids. */
    @JvmStatic
    @ZenMethod
    fun addSpawnMobs(id: String, weight: Int, radius: Int, cost: Int, min: Int, max: Int, entities: Array<String>) =
        add(id, weight, radius, cost, CriticalAction.SpawnMobs(entities.map(layer::parse), min, max))

    /** A dragon breath cloud with the harm effect. */
    @JvmStatic
    @ZenMethod
    fun addCloud(id: String, weight: Int, radius: Int, cost: Int, cloudRadius: Float, durationTicks: Int) =
        add(id, weight, radius, cost, CriticalAction.Cloud(cloudRadius, durationTicks, PotionEffect(MobEffects.INSTANT_DAMAGE)))

    @JvmStatic @ZenMethod fun remove(id: String) = act("Removing the End Reforked critical event $id") { layer.remove(layer.parse(id)) }

    @JvmStatic @ZenMethod fun removeAll() = act("Removing all End Reforked critical events") { layer.removeAll() }
}
