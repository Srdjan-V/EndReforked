package io.github.srdjanv.endreforked.api.worldgen

import io.github.srdjanv.endreforked.api.registry.RegistryEntry
import net.minecraft.block.state.IBlockState
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.BiomeDictionary

/*
 * The world feature registry: ores, generic features and structures that generate with new chunks.
 * Server only, clients never read it. It uses Minecraft types where that is simpler than ids.
 * A state used as a match (`replace`) must equal the state in the world, so a block with one state matches all of it.
 */

/** The run order in a chunk, and nothing else. Within a kind the features run by descending [WorldFeature.weight], then by id. */
enum class FeatureKind { ORE, GENERIC, STRUCTURE }

/** Where a structure is placed with a grid of [spacing] chunks, like a vanilla village. [uniqueId] seeds the random position in each cell. */
data class Spacing(val uniqueId: Int, val spacing: Int, val separation: Int)

/**
 * The numbers of a placement. A [WorldFeature] has one default and each condition key can carry its own. They are never merged, the first that fits is used whole.
 * The feature reads only what it needs. [rarity] 0 means always, else the feature runs in 1 of [rarity] chunks.
 * [spacing] makes a structure appear at most once per grid cell.
 */
data class GenParams(
    val minHeight: Int,
    val maxHeight: Int,
    val rarity: Int = 0,
    val amount: Int = 0,
    val radius: Int = 0,
    val fillRadius: Int = 0,
    val spacing: Spacing? = null,
)

/**
 * One kind of key of the conditions: dimension id, biome id or biome type.
 * A non-empty [allow] lets only its keys pass. A key in [deny] never passes. A value of [allow] replaces the default [GenParams], null keeps them.
 * For biome types the order of [allow] decides which value wins when a biome has several of the types.
 */
data class Condition<T : Any>(val allow: Map<T, GenParams?> = emptyMap(), val deny: Set<T> = emptySet()) {
    companion object {
        fun <T : Any> of(vararg keys: T) = Condition(keys.associateWith { null })
    }
}

/** A step of a locator. A locator starts at a position and applies its steps in order. A position that cannot be found stops the feature. */
sealed interface Step {
    /** Adds the offsets. */
    data class Move(val dx: Int, val dy: Int = 0, val dz: Int = 0) : Step

    /** Picks a random height from the min and max height of the params. */
    data object RandomHeight : Step

    /** Searches downwards from the top of the world to the min height for the first block that is not air, and that equals [on] when it is set. */
    data class Surface(val on: IBlockState? = null) : Step
}

/** A plant of the Organa flower that grows on the surface block the locator finds. [chance] is in percent. */
data class OrganaFlower(val locator: List<Step>, val chance: Int = 100)

/** What a feature places. Each variant has its own fixed way to search the start position. */
sealed interface Feature {
    /** A vein of [block] with `amount` blocks that replaces [replace]. Starts at the middle of the chunk, at a random height. */
    data class Ore(val block: IBlockState, val replace: IBlockState) : Feature

    /**
     * A sphere with `radius` that replaces the blocks of [replace]. Each one changes in [chance] percent.
     * [alt] replaces [block] in [altChance] percent of the changes.
     */
    data class Sphere(
        val replace: List<IBlockState>,
        val block: IBlockState,
        val chance: Int = 81,
        val alt: IBlockState? = null,
        val altChance: Int = 0,
    ) : Feature

    /** Like [Sphere]. Inside `fillRadius` every block of [replace] becomes [inner], outside it becomes [outer] in [outerChance] percent. */
    data class FilledSphere(
        val replace: List<IBlockState>,
        val inner: IBlockState,
        val outer: IBlockState,
        val outerChance: Int = 81,
    ) : Feature

    /**
     * A disc with `radius` on the surface. A surface block that equals [replace] becomes [ground] in [chance] percent.
     * Then [plant] grows above it in [plantChance] percent, and the ground grows (it must be an `IGrowable`) in [growChance] percent of those.
     */
    data class RadiusSurface(
        val replace: IBlockState,
        val ground: IBlockState,
        val plant: IBlockState,
        val chance: Int = 81,
        val plantChance: Int = 31,
        val growChance: Int = 6,
    ) : Feature

    /** A shallow fluid lake carved into the top surface of matching blocks. */
    data class Lake(
        val replace: List<IBlockState>,
        val top: IBlockState,
        val shell: IBlockState,
        val fluid: IBlockState,
        val depth: Int = 2,
    ) : Feature

    /** `amount` plants of [block] (a `BlockBush`) on the surface in the area. With [randomAge] a crop starts with a random age below its maximum. */
    data class Bush(val block: IBlockState, val randomAge: Boolean = false) : Feature

    /**
     * A hollow geode with `radius` at a random height. Of the blocks of [replace] in the sphere, the outer layer becomes [shell],
     * the next one [lining], and the inside becomes air (see `geodeLayer`). Then [markerAmount] plants of [marker] (a `BlockBush`) grow on the surface
     * above the centre, in a disc of 3 blocks, so that the geode can be found. Without a [marker] there are none.
     */
    data class Geode(
        val shell: IBlockState,
        val lining: IBlockState,
        val replace: List<IBlockState>,
        val marker: IBlockState? = null,
        val markerAmount: Int = 0,
    ) : Feature

    /** Organa flower growth, resolved after terrain-changing features. */
    data object FlowerGeneration : Feature

    /** A structure template of the structure folder, placed in one chunk. After it, the [flowers] try to grow. */
    data class Template(
        val structure: ResourceLocation,
        val locator: List<Step> = listOf(Step.RandomHeight),
        val flowers: List<OrganaFlower> = emptyList(),
    ) : Feature
}

/**
 * Something that generates with new chunks.
 * It generates in a chunk when the dimension passes [dimensions], and the biome in the middle of the chunk passes [biomes] and [biomeTypes].
 * A deny always wins. Without any allow key of biomes and biome types, every biome passes.
 * The numbers are the first that fit: biome, then biome type, then dimension, then [params]. Without any, the feature does not run.
 */
data class WorldFeature(
    override val id: ResourceLocation,
    val kind: FeatureKind,
    val weight: Int,
    val feature: Feature,
    val params: GenParams? = null,
    val dimensions: Condition<Int> = Condition(),
    val biomes: Condition<ResourceLocation> = Condition(),
    val biomeTypes: Condition<BiomeDictionary.Type> = Condition(),
) : RegistryEntry

/** A biome that the mod adds to The End through Nether API. The [id] is the id of the biome. The weight is the Nether API weight. */
data class EndBiome(override val id: ResourceLocation, val weight: Int, val enabled: Boolean = true) : RegistryEntry
