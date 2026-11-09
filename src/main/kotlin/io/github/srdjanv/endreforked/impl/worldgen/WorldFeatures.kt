package io.github.srdjanv.endreforked.impl.worldgen

import io.github.srdjanv.endreforked.api.worldgen.EndBiome
import io.github.srdjanv.endreforked.api.worldgen.Feature
import io.github.srdjanv.endreforked.api.worldgen.FeatureKind
import io.github.srdjanv.endreforked.api.worldgen.GenParams
import io.github.srdjanv.endreforked.api.worldgen.WorldFeature
import io.github.srdjanv.endreforked.impl.registry.FrozenRegistry
import io.github.srdjanv.endreforked.impl.registry.ManagedRegistry
import net.minecraft.block.BlockBush
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.BiomeDictionary

/** The world features, grouped by kind in run order. No client has this registry. */
class WorldFeatures(entries: Collection<WorldFeature>) : FrozenRegistry<WorldFeature>(KEY, entries) {
    private val byKind: Map<FeatureKind, List<WorldFeature>> = this.entries.sortedWith(ORDER).groupBy { it.kind }

    /** The features of [kind]: the highest weight first, equal weights by id. */
    fun of(kind: FeatureKind): List<WorldFeature> = byKind[kind] ?: emptyList()

    companion object {
        val KEY = ResourceLocation("endreborn", "world_features")

        private val ORDER = compareByDescending<WorldFeature> { it.weight }.thenBy { it.id }

        fun managed(defaults: () -> Collection<WorldFeature>, extraValidation: (WorldFeature) -> String? = { null }) =
            ManagedRegistry(KEY, defaults, { validate(it) ?: extraValidation(it) }, ::WorldFeatures)

        /** Checks the numbers that the feature needs, in the default params and in every override. */
        fun validate(f: WorldFeature): String? {
            val all = listOfNotNull(f.params) + (f.dimensions.allow.values + f.biomes.allow.values + f.biomeTypes.allow.values).filterNotNull()
            if (all.isEmpty()) return "has no params"
            all.firstNotNullOfOrNull { paramsProblem(f.feature, it) }?.let { return it }
            return featureProblem(f.feature)
        }

        private fun paramsProblem(feature: Feature, p: GenParams): String? {
            if (p.minHeight < 0 || p.maxHeight < p.minHeight) return "needs 0 <= minHeight <= maxHeight"
            if (p.rarity < 0) return "rarity must not be negative"
            p.spacing?.let { if (it.separation < 0 || it.spacing <= it.separation) return "needs 0 <= separation < spacing" }
            return when (feature) {
                is Feature.Ore, is Feature.Bush, Feature.FlowerGeneration -> if (p.amount < 1) "amount must be at least 1" else null
                is Feature.Sphere, is Feature.RadiusSurface, is Feature.Lake -> if (p.radius < 1) "radius must be at least 1" else null
                is Feature.Geode -> if (p.radius < 3) "a geode needs a radius of at least 3" else null
                is Feature.FilledSphere -> if (p.radius < 1 || p.fillRadius < 0) "needs radius >= 1 and fillRadius >= 0" else null
                is Feature.Template -> null
            }
        }

        private fun featureProblem(feature: Feature): String? {
            fun bad(vararg percent: Int) = percent.any { it !in 0..100 }
            return when (feature) {
                is Feature.Sphere ->
                    if (feature.replace.isEmpty()) "replace must not be empty" else if (bad(feature.chance, feature.altChance)) "a chance is not in 0..100" else null
                is Feature.FilledSphere ->
                    if (feature.replace.isEmpty()) "replace must not be empty" else if (bad(feature.outerChance)) "a chance is not in 0..100" else null
                is Feature.RadiusSurface -> if (bad(feature.chance, feature.plantChance, feature.growChance)) "a chance is not in 0..100" else null
                is Feature.Lake -> if (feature.depth < 1) "lake depth must be at least 1" else null
                is Feature.Geode -> when {
                    feature.replace.isEmpty() -> "replace must not be empty"
                    feature.markerAmount < 0 -> "markerAmount must not be negative"
                    feature.marker != null && feature.marker.block !is BlockBush -> "${feature.marker.block.registryName} is not a bush"
                    else -> null
                }
                is Feature.Bush -> if (feature.block.block !is BlockBush) "${feature.block.block.registryName} is not a bush" else null
                Feature.FlowerGeneration -> null
                is Feature.Template ->
                    if (feature.locator.isEmpty()) "the locator has no steps"
                    else if (feature.flowers.any { it.locator.isEmpty() || bad(it.chance) }) "a flower has no steps or a chance outside 0..100"
                    else null
                is Feature.Ore -> null
            }
        }
    }
}

/** The layers of a geode, from the outside to the inside. */
enum class GeodeLayer { SHELL, LINING, AIR }

/** The layer of a geode block that is [distance] from the centre of a geode with [radius]: the outer block layer is the shell, the next the lining, the rest air. */
fun geodeLayer(distance: Double, radius: Int): GeodeLayer = when {
    distance > radius - 1 -> GeodeLayer.SHELL
    distance > radius - 2 -> GeodeLayer.LINING
    else -> GeodeLayer.AIR
}

/** True when the feature may generate in this dimension. */
fun WorldFeature.allowsDimension(dimension: Int): Boolean =
    dimension !in dimensions.deny && (dimensions.allow.isEmpty() || dimension in dimensions.allow)

/** True when the feature may generate in this biome, which has these biome types. */
fun WorldFeature.allowsBiome(biome: ResourceLocation, types: Collection<BiomeDictionary.Type>): Boolean {
    if (biome in biomes.deny || types.any { it in biomeTypes.deny }) return false
    if (biomes.allow.isEmpty() && biomeTypes.allow.isEmpty()) return true
    return biome in biomes.allow || types.any { it in biomeTypes.allow }
}

/**
 * The numbers that apply here, or null when the feature does not generate here (dimension or biome not allowed, or no numbers).
 * The first that fits wins, in this order: biome, biome type, dimension, default.
 * Of several biome types of the biome, the first in the order of the condition wins. A key without own params does not win.
 */
fun WorldFeature.paramsAt(dimension: Int, biome: ResourceLocation, types: Collection<BiomeDictionary.Type>): GenParams? {
    if (!allowsDimension(dimension) || !allowsBiome(biome, types)) return null
    return biomes.allow[biome]
        ?: biomeTypes.allow.entries.firstOrNull { it.value != null && it.key in types }?.value
        ?: dimensions.allow[dimension]
        ?: params
}

/** The biomes that the mod adds to The End. */
class EndBiomes(entries: Collection<EndBiome>) : FrozenRegistry<EndBiome>(KEY, entries) {
    companion object {
        val KEY = ResourceLocation("endreborn", "end_biomes")

        fun managed(defaults: () -> Collection<EndBiome>, extraValidation: (EndBiome) -> String? = { null }) =
            ManagedRegistry(KEY, defaults, { validate(it) ?: extraValidation(it) }, ::EndBiomes)

        fun validate(b: EndBiome): String? = if (b.weight < 1) "weight must be at least 1" else null
    }
}
