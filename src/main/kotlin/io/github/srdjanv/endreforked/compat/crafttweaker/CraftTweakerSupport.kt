package io.github.srdjanv.endreforked.compat.crafttweaker

import crafttweaker.CraftTweakerAPI
import crafttweaker.IAction
import crafttweaker.api.block.IBlockState
import crafttweaker.api.block.IBlockStateMatcher
import crafttweaker.api.liquid.ILiquidStack
import crafttweaker.api.minecraft.CraftTweakerMC
import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import io.github.srdjanv.endreforked.api.world.BlockMatch
import io.github.srdjanv.endreforked.compat.scripting.ScriptLayer
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import net.minecraftforge.fluids.Fluid

/*
 * The CraftTweaker adapter: the ZenScript classes of `mods.endreborn.*`. CraftTweaker finds them by itself (`@ZenRegister` in its ASM scan),
 * and nothing in the mod loads them, so without CraftTweaker they never load.
 * Scripts: the default `crafttweaker` loader. CraftTweaker runs it in the registry event of the recipes, before init. The registries are built later,
 * in the load complete phase, so every call only records an operation in the script layer. CraftTweaker has no script reload, so nothing is reset or flushed.
 * Each call is an IAction that CraftTweakerAPI.apply runs at once. An invalid argument is logged with logError and the call does nothing.
 */

/** The script layers of CraftTweaker, one per registry. Created at the first script call, when the registries exist. */
internal object CtLayers {
    /** The namespace of the ids that scripts get without a name. */
    private const val NAMESPACE = "crafttweaker"
    private val sync get() = EndReforked.getProxy().registrySync
    private val registries get() = sync.registries

    private fun <E : RegistryEntry> layer(registry: ManagedRegistry<E, *>) = ScriptLayer(registry, NAMESPACE, sync::report)

    val machines by lazy { layer(registries.machineRecipes) }
    val wand by lazy { layer(registries.wandConversions) }
    val fluidBlocks by lazy { layer(registries.fluidBlockInteractions) }
    val fluidEntities by lazy { layer(registries.fluidEntityInteractions) }
    val worldFeatures by lazy { layer(registries.worldFeatures) }
    val endBiomes by lazy { layer(registries.endBiomes) }
    val criticalEvents by lazy { layer(registries.criticalEvents) }
}

/** Runs [run] as a CraftTweaker action. */
internal fun act(description: String, run: () -> Unit) = CraftTweakerAPI.apply(object : IAction {
    override fun apply() = run()

    override fun describe() = description
})

/** Logs a script error. A check ends with `return fail(...)`. */
internal fun fail(message: String) = CraftTweakerAPI.logError(message)

internal fun fluidOf(liquid: ILiquidStack?): Fluid? = CraftTweakerMC.getLiquidStack(liquid)?.fluid

/** A block state matches exactly. Another matcher (for example `<blockstate:minecraft:log>.matchBlock()`) matches the properties that have one value in all its states. */
internal fun matchOf(matcher: IBlockStateMatcher?): BlockMatch? {
    if (matcher == null) return null
    if (matcher is IBlockState) return BlockMatch(CraftTweakerMC.getBlockState(matcher))
    val states = matcher.matchingBlockStates.map { CraftTweakerMC.getBlockState(it) }
    val first = states.firstOrNull() ?: return null
    if (states.any { it.block !== first.block }) return null
    return BlockMatch(first.block, first.properties.filter { (p, v) -> states.all { it.properties[p] == v } })
}
