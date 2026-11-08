package io.github.srdjanv.endreforked.compat.scripting

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.registry.LoadReport
import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.api.world.FluidEntityInteraction
import io.github.srdjanv.endreforked.api.world.WandConversion
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import net.minecraft.block.state.IBlockState
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.Fluid

/**
 * The part of the script edge that every script engine shares (GroovyScript and CraftTweaker).
 * It sits between one script engine and the script layer of one registry. The engine passes Minecraft objects and builds the entries.
 *
 * - Owner: the namespace is also the owner of the operations in the registry. [reset] drops only the operations of this engine, so a `/gs reload` keeps the operations of CraftTweaker.
 * - Ids: an entry added without an id gets `namespace:prefix_N`. N counts up from 1 and starts again at [reset],
 *   so the same script gives the same ids after every reload. The namespace is one per engine, so two engines never collide.
 * - Lifecycle: operations are only recorded. [reset] drops them before a script run, [flush] rebuilds once after it, and only if something changed.
 *   Before the first rebuild of the registry [flush] does nothing: the first rebuild is the one of the mod, after the scripts of the startup.
 */
class ScriptLayer<E : RegistryEntry>(
    private val registry: ManagedRegistry<E, *>,
    private val namespace: String,
    private val onRebuilt: (name: String, report: LoadReport) -> Unit,
) {
    val name: String get() = registry.key.path
    private var counter = 0
    private var hasOps = false
    private var pending = false

    private fun changed() {
        hasOps = true
        pending = true
    }

    /** An id from a script: a full `namespace:path`, or only a path in the namespace of the mod. */
    fun parse(id: String) = if (':' in id) ResourceLocation(id) else ResourceLocation(Tags.MODID, id)

    /** Adds the entry that [build] makes. Without an [id] it gets a generated one. Returns the entry. */
    fun add(id: ResourceLocation?, prefix: String = name, build: (ResourceLocation) -> E): E {
        val entry = build(id ?: ResourceLocation(namespace, "${prefix}_${++counter}"))
        registry.add(entry, namespace)
        changed()
        return entry
    }

    fun remove(id: ResourceLocation) {
        registry.remove(id, namespace)
        changed()
    }

    fun removeIf(predicate: (E) -> Boolean) {
        registry.removeIf(namespace, predicate)
        changed()
    }

    fun removeAll() = removeIf { true }

    /** Drops the recorded operations of this engine. Call it before the scripts run again. The next [flush] rebuilds. */
    fun reset() {
        counter = 0
        if (!hasOps) return
        registry.clear(namespace)
        hasOps = false
        pending = true
    }

    /** Rebuilds the registry when operations changed since the last call, and returns the report. Call it once after the scripts ran. */
    fun flush(): LoadReport? {
        if (!pending) return null
        pending = false
        if (!registry.isFrozen) return null
        return registry.rebuild().also { onRebuilt(name, it) }
    }
}

/** Removals with Minecraft types, for every script engine. A match on a block state needs the same block and the same value of every property of the state. */
@JvmName("wandByInput")
fun ScriptLayer<WandConversion>.removeByInput(state: IBlockState) = removeIf { it.match.matches(state) }

@JvmName("wandByOutput")
fun ScriptLayer<WandConversion>.removeByOutput(state: IBlockState) = removeIf { it.result == state }

@JvmName("fluidBlockByFluid")
fun ScriptLayer<FluidBlockInteraction>.removeByFluid(fluid: Fluid) = removeIf { it.fluid == fluid }

@JvmName("fluidBlockByInput")
fun ScriptLayer<FluidBlockInteraction>.removeByInput(state: IBlockState) = removeIf { it.match.matches(state) }

@JvmName("fluidBlockByOutput")
fun ScriptLayer<FluidBlockInteraction>.removeByOutput(state: IBlockState) = removeIf { it.result == state }

@JvmName("fluidEntityByFluid")
fun ScriptLayer<FluidEntityInteraction>.removeByFluid(fluid: Fluid) = removeIf { it.fluid == fluid }

fun ScriptLayer<FluidEntityInteraction>.removeByEntity(entity: ResourceLocation) = removeIf { it.entity == entity }
