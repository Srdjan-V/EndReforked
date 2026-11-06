package io.github.srdjanv.endreforked.compat.groovyscript

import com.cleanroommc.groovyscript.api.GroovyLog
import com.cleanroommc.groovyscript.helper.Alias
import io.github.srdjanv.endreforked.api.worldgen.Condition
import io.github.srdjanv.endreforked.api.worldgen.EndBiome
import io.github.srdjanv.endreforked.api.worldgen.Feature
import io.github.srdjanv.endreforked.api.worldgen.FeatureKind
import io.github.srdjanv.endreforked.api.worldgen.GenParams
import io.github.srdjanv.endreforked.api.worldgen.WorldFeature
import io.github.srdjanv.endreforked.compat.scripting.ScriptLayer
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.util.ResourceLocation

/**
 * `mods.endreborn.world_feature`: what generates with new chunks. The server only, so a reload affects only new chunks.
 * Scripts can add ores. The other kinds (spheres, bushes, structure templates) need a builder each, later.
 */
class WorldFeatureGroovy(layer: ScriptLayer<WorldFeature>) : ReloadableGroovy<WorldFeature>(Alias.generateOf("WorldFeature"), layer) {
    fun oreBuilder() = OreBuilder(this)

    /** Removes the feature with this id, for example `endreborn:obsidian_essence_ore`. */
    fun remove(id: String) = layer.remove(layer.parse(id))

    fun removeAll() = layer.removeAll()
}

/** A vein of a block that replaces another block. It starts in the middle of the chunk, at a random height. */
class OreBuilder(private val registry: WorldFeatureGroovy) {
    private var name: ResourceLocation? = null
    private var block: IBlockState? = null
    private var replace: IBlockState? = null
    private var minHeight = 0
    private var maxHeight = 255
    private var amount = 0
    private var rarity = 0
    private var weight = 0
    private var dimensions = emptyList<Int>()

    fun name(name: String) = apply { this.name = registry.layer.parse(name) }

    /** The ore. */
    fun block(state: IBlockState) = apply { block = state }

    fun block(block: Block) = apply { this.block = block.defaultState }

    /** The block that the ore replaces. A state must equal the state in the world, a block replaces every state of it. */
    fun replace(state: IBlockState) = apply { replace = state }

    fun replace(block: Block) = apply { replace = block.defaultState }

    fun minHeight(height: Int) = apply { minHeight = height }

    fun maxHeight(height: Int) = apply { maxHeight = height }

    /** The size of a vein. */
    fun amount(amount: Int) = apply { this.amount = amount }

    /** 0 is every chunk, else 1 of this many chunks. */
    fun rarity(rarity: Int) = apply { this.rarity = rarity }

    /** The run order: the highest weight runs first. */
    fun weight(weight: Int) = apply { this.weight = weight }

    /** Only these dimension ids (0 overworld, 1 the End). Without it every dimension. */
    fun dimensions(vararg ids: Int) = apply { dimensions = ids.toList() }

    fun register(): WorldFeature? {
        val msg = GroovyLog.msg("Error adding an ore feature").error()
        msg.add(block == null, "needs a block")
        msg.add(replace == null, "needs a block to replace")
        msg.add(amount < 1, "amount must be at least 1, got {}", amount)
        if (msg.postIfNotEmpty()) return null
        return registry.layer.add(name, "ore") {
            WorldFeature(
                it, FeatureKind.ORE, weight, Feature.Ore(block!!, replace!!),
                GenParams(minHeight, maxHeight, rarity, amount),
                dimensions = if (dimensions.isEmpty()) Condition() else Condition.of(*dimensions.toTypedArray()),
            )
        }
    }
}

/**
 * `mods.endreborn.end_biome`: the biomes that the mod adds to The End. Applied when the server starts, so only the startup run of the scripts counts.
 * A later `/gs reload` rebuilds the registry and logs a warning, the running game keeps its biomes.
 */
class EndBiomeGroovy(layer: ScriptLayer<EndBiome>) : ReloadableGroovy<EndBiome>(Alias.generateOf("EndBiome"), layer) {
    /** Sets the weight of the biome (the id is `namespace:path`) and enables it. Adds the biome when the mod has none with this id. */
    fun set(biome: String, weight: Int) = put(biome, weight, true)

    /** Keeps the biome out of The End. */
    fun disable(biome: String) = put(biome, 1, false)

    fun remove(biome: String) = layer.remove(layer.parse(biome))

    private fun put(biome: String, weight: Int, enabled: Boolean) = layer.add(layer.parse(biome), "end_biome") { EndBiome(it, weight, enabled) }
}
