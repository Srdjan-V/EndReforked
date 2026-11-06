package io.github.srdjanv.endreforked.compat.groovyscript

import com.cleanroommc.groovyscript.api.GroovyLog
import com.cleanroommc.groovyscript.helper.Alias
import io.github.srdjanv.endreforked.api.entropy.CriticalAction
import io.github.srdjanv.endreforked.api.entropy.CriticalEvent
import io.github.srdjanv.endreforked.compat.scripting.ScriptLayer
import net.minecraft.init.MobEffects
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation

/**
 * `mods.endreborn.critical_events`: what happens in a critical chunk. The server only.
 * `recipeBuilder().weight(5).radius(3).cost(40).convert().register()`, or `.spawn('endreborn:watcher', 1, 3)`, or `.cloud(4, 200)`.
 */
class CriticalEventGroovy(layer: ScriptLayer<CriticalEvent>) : ReloadableGroovy<CriticalEvent>(Alias.generateOf("CriticalEvents"), layer) {
    fun recipeBuilder() = CriticalEventBuilder(this)

    /** Removes the event with this id, for example `endreborn:critical/burst`. */
    fun remove(id: String) = layer.remove(layer.parse(id))

    fun removeAll() = layer.removeAll()
}

class CriticalEventBuilder(private val registry: CriticalEventGroovy) {
    private var name: ResourceLocation? = null
    private var weight = 1
    private var radius = 0
    private var cost = 0
    private var action: CriticalAction? = null

    fun name(name: String) = apply { this.name = registry.layer.parse(name) }

    fun weight(weight: Int) = apply { this.weight = weight }

    /** The radius in blocks. */
    fun radius(radius: Int) = apply { this.radius = radius }

    /** The entropy that the chunk loses. */
    fun cost(cost: Int) = apply { this.cost = cost }

    /** Converts blocks with the wand conversions. */
    fun convert() = apply { action = CriticalAction.Convert }

    /** Spawns [min] to [max] mobs of these entity ids. */
    fun spawn(min: Int, max: Int, vararg entities: String) = apply {
        action = CriticalAction.SpawnMobs(entities.map(registry.layer::parse), min, max)
    }

    /** A dragon breath cloud with the harm effect. */
    fun cloud(radius: Float, durationTicks: Int) = apply {
        action = CriticalAction.Cloud(radius, durationTicks, PotionEffect(MobEffects.INSTANT_DAMAGE))
    }

    /** A dragon breath cloud with another effect. */
    fun cloud(radius: Float, durationTicks: Int, potion: String, amplifier: Int, effectTicks: Int) = apply {
        val p = Potion.REGISTRY.getObject(registry.layer.parse(potion))
        if (p == null) GroovyLog.msg("Unknown potion {}", potion).error().post()
        else action = CriticalAction.Cloud(radius, durationTicks, PotionEffect(p, effectTicks, amplifier))
    }

    fun register(): CriticalEvent? {
        val msg = GroovyLog.msg("Error adding a critical event").error()
        msg.add(action == null, "needs an action: convert, spawn or cloud")
        if (msg.postIfNotEmpty()) return null
        return registry.layer.add(name, "critical") { CriticalEvent(it, weight, radius, cost, action!!) }
    }
}
