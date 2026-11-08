package io.github.srdjanv.endreforked.compat.crafttweaker

import crafttweaker.annotations.ZenRegister
import crafttweaker.api.block.IBlockState
import crafttweaker.api.minecraft.CraftTweakerMC
import io.github.srdjanv.endreforked.api.worldgen.Condition
import io.github.srdjanv.endreforked.api.worldgen.EndBiome
import io.github.srdjanv.endreforked.api.worldgen.Feature
import io.github.srdjanv.endreforked.api.worldgen.FeatureKind
import io.github.srdjanv.endreforked.api.worldgen.GenParams
import io.github.srdjanv.endreforked.api.worldgen.WorldFeature
import stanhebben.zenscript.annotations.Optional
import stanhebben.zenscript.annotations.ZenClass
import stanhebben.zenscript.annotations.ZenMethod

/**
 * `mods.endreborn.WorldFeature`: what generates with new chunks. Scripts can add ores. The other kinds (spheres, bushes, structure templates) are not supported.
 */
@ZenRegister
@ZenClass("mods.endreborn.WorldFeature")
object WorldFeatureZen {
    private val layer get() = CtLayers.worldFeatures

    /**
     * A vein of [block] that replaces [replace] (the state must equal the state in the world). It starts in the middle of the chunk, at a random height.
     * [amount] is the size of a vein. [rarity] 0 is every chunk, else 1 of this many chunks. The feature with the highest [weight] runs first.
     * [dimensions] are dimension ids (0 overworld, 1 the End), without them every dimension.
     */
    @JvmStatic
    @ZenMethod
    fun addOre(
        block: IBlockState, replace: IBlockState, amount: Int,
        @Optional minHeight: Int, @Optional(valueLong = 255) maxHeight: Int, @Optional rarity: Int, @Optional weight: Int,
        @Optional dimensions: IntArray?, @Optional name: String?,
    ) {
        if (amount < 1) return fail("WorldFeature: amount must be at least 1, got $amount")
        val ore = Feature.Ore(CraftTweakerMC.getBlockState(block), CraftTweakerMC.getBlockState(replace))
        act("Adding an End Reforked ore feature") {
            layer.add(name?.let(layer::parse), "ore") {
                WorldFeature(
                    it, FeatureKind.ORE, weight, ore, GenParams(minHeight, maxHeight, rarity, amount),
                    dimensions = if (dimensions == null || dimensions.isEmpty()) Condition() else Condition.of(*dimensions.toTypedArray()),
                )
            }
        }
    }

    /** Removes the feature with this id, for example `endreborn:obsidian_essence_ore`. */
    @JvmStatic @ZenMethod fun remove(id: String) = act("Removing the End Reforked world feature $id") { layer.remove(layer.parse(id)) }

    @JvmStatic @ZenMethod fun removeAll() = act("Removing all End Reforked world features") { layer.removeAll() }
}

/** `mods.endreborn.EndBiome`: the biomes that the mod adds to The End. */
@ZenRegister
@ZenClass("mods.endreborn.EndBiome")
object EndBiomeZen {
    private val layer get() = CtLayers.endBiomes

    /** Sets the weight of the biome (the id is `namespace:path`) and enables it. Adds the biome when the mod has none with this id. */
    @JvmStatic @ZenMethod fun set(biome: String, weight: Int) = put("Setting the weight of", biome, weight, true)

    /** Keeps the biome out of The End. */
    @JvmStatic @ZenMethod fun disable(biome: String) = put("Disabling", biome, 1, false)

    @JvmStatic @ZenMethod fun remove(biome: String) = act("Removing the End Reforked biome entry $biome") { layer.remove(layer.parse(biome)) }

    private fun put(verb: String, biome: String, weight: Int, enabled: Boolean) =
        act("$verb the End Reforked biome $biome") { layer.add(layer.parse(biome), "end_biome") { EndBiome(it, weight, enabled) } }
}
