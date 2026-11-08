package io.github.srdjanv.endreforked.common.registry

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.worldgen.Condition
import io.github.srdjanv.endreforked.api.worldgen.EndBiome
import io.github.srdjanv.endreforked.api.worldgen.Feature
import io.github.srdjanv.endreforked.api.worldgen.FeatureKind
import io.github.srdjanv.endreforked.api.worldgen.GenParams
import io.github.srdjanv.endreforked.api.worldgen.OrganaFlower
import io.github.srdjanv.endreforked.api.worldgen.Spacing
import io.github.srdjanv.endreforked.api.worldgen.Step
import io.github.srdjanv.endreforked.api.worldgen.WorldFeature
import io.github.srdjanv.endreforked.common.ModBioms
import io.github.srdjanv.endreforked.common.ModBlocks
import net.minecraft.block.Block
import net.minecraft.block.BlockStone
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.BiomeDictionary

/**
 * The world features and End biomes that ship with the mod. Called when the registries are built, after the blocks exist.
 * A feature whose block is disabled by the disabled content config is left out.
 * Within a kind the order of the lists is the order of the weights.
 */
object WorldFeatureDefaults {
    private const val END = 1
    private const val OVERWORLD = 0

    private fun id(name: String) = ResourceLocation(Tags.MODID, name)

    private fun mc(path: String) = ResourceLocation("minecraft", path)

    private fun state(block: Block?): IBlockState? = block?.defaultState

    private fun params(min: Int, max: Int, rarity: Int = 0, amount: Int = 0, radius: Int = 0, fill: Int = 0, spacing: Spacing? = null) =
        GenParams(min, max, rarity, amount, radius, fill, spacing)

    /** Dimension, with its own params. */
    private fun dims(vararg d: Pair<Int, GenParams?>) = Condition(mapOf(*d))

    private fun biomes(vararg b: Pair<ResourceLocation, GenParams?>) = Condition(mapOf(*b))

    private fun ordered(kind: FeatureKind, entries: List<WorldFeature?>): List<WorldFeature> {
        val present = entries.filterNotNull()
        return present.mapIndexed { i, f -> f.copy(kind = kind, weight = present.size - i) }
    }

    fun all(): List<WorldFeature> = ores() + generic() + structures()

    private fun ores(): List<WorldFeature> {
        fun ore(name: String, block: Block?, replace: IBlockState, params: GenParams? = null, dims: Condition<Int> = Condition(), biomes: Condition<ResourceLocation> = Condition()) =
            state(block)?.let { WorldFeature(id(name), FeatureKind.ORE, 0, Feature.Ore(it, replace), params, dims, biomes) }
        val diorite = Blocks.STONE.defaultState.withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE)
        return ordered(FeatureKind.ORE, listOf(
            ore("obsidian_essence_ore", ModBlocks.OBSIDIAN_ESSENCE_ORE, Blocks.OBSIDIAN.defaultState,
                dims = dims(OVERWORLD to params(0, 256, rarity = 80, amount = 20), END to params(0, 256, rarity = 60, amount = 40))),
            ore("entropy_ore_crop", ModBlocks.ENTROPY_CROP_BLOCK, Blocks.END_STONE.defaultState,
                dims = dims(END to params(0, 60, rarity = 25, amount = 20))),
            ore("tungsten_ore", ModBlocks.TUNGSTEN_ORE, diorite,
                dims = dims(OVERWORLD to params(0, 48, rarity = 15, amount = 10))),
            ore("tungsten_end", ModBlocks.TUNGSTEN_END_ORE, Blocks.END_STONE.defaultState,
                params = params(0, 48, rarity = 5, amount = 25), dims = Condition.of(END), biomes = Condition.of(mc("sky"))),
        ))
    }

    private fun generic(): List<WorldFeature> {
        val organa = ModBioms.ORGANA_BIOME.registryName!!
        val endStone = Blocks.END_STONE.defaultState
        val moss = ModBlocks.ORGANA_END_STONE_BLOCK.defaultState
        val magma = ModBlocks.END_MAGMA_BLOCK.defaultState
        val fluidMagma = ModBlocks.FLUID_END_MAGMA_BLOCK.defaultState
        fun generic(name: String, feature: Feature?, dims: Condition<Int> = Condition(), biomes: Condition<ResourceLocation> = Condition()) =
            feature?.let { WorldFeature(id(name), FeatureKind.GENERIC, 0, it, null, dims, biomes) }
        fun cropBush(block: Block?) = block?.let { Feature.Bush(it.defaultState, randomAge = true) }
        return ordered(FeatureKind.GENERIC, listOf(
            generic("lormyte", state(ModBlocks.LORMYTE_CRYSTAL_BLOCK)?.let { Feature.Sphere(listOf(endStone, moss), it) },
                dims(END to params(20, 45, rarity = 80, radius = 12))),
            generic("fuzzy_end_magma", Feature.Sphere(listOf(endStone, moss), magma, alt = fluidMagma, altChance = 51),
                dims(END to params(10, 25, rarity = 95, radius = 12))),
            generic("filled_end_magma", Feature.FilledSphere(listOf(endStone, moss), inner = fluidMagma, outer = magma),
                biomes = biomes(organa to params(10, 25, rarity = 95, radius = 12, fill = 6))),
            generic("entropy_end_stone", Feature.Ore(ModBlocks.ENTROPY_END_STONE.defaultState, endStone),
                dims(END to params(10, 30, rarity = 25, amount = 30))),
            generic("organa_weed",
                Feature.RadiusSurface(endStone, ModBlocks.ORGANA_BLOCK.defaultState, ModBlocks.ORGANA_WEED_BLOCK.defaultState),
                dims(END to params(50, 90, rarity = 25, radius = 4))),
            generic("organa_spores",
                Feature.Bush(ModBlocks.ORGANA_SPORE_BLOCK.defaultState),
                biomes = biomes(organa to params(50, 130, amount = 8))),
            generic("organa_spore_colony",
                Feature.RadiusSurface(
                    ModBlocks.ORGANA_BLOCK.defaultState,
                    ModBlocks.ORGANA_BLOCK.defaultState,
                    ModBlocks.ORGANA_SPORE_BLOCK.defaultState,
                    chance = 45,
                    plantChance = 35,
                    growChance = 0,
                ),
                biomes = biomes(organa to params(50, 130, rarity = 3, radius = 3))),
            generic("end_coral", Feature.Bush(ModBlocks.END_CORAL.defaultState),
                dims(END to params(50, 90, rarity = 0, amount = 20))),
            generic("dragonite_crop", cropBush(ModBlocks.DRAGONITE_CROP), dims(END to params(50, 90, rarity = 5, amount = 6))),
            generic("end_flower", cropBush(ModBlocks.VOIDBLOOM_CROP), dims(END to params(50, 90, rarity = 5, amount = 6))),
        ))
    }

    private fun structures(): List<WorldFeature> {
        val grass = ModBlocks.ORGANA_BLOCK.defaultState
        val islandBiomes = Condition.of(mc("sky"), mc("plains"), mc("desert"), mc("ocean"), mc("deep_ocean"), mc("savanna"))
        fun grid(name: String, spacing: Int, separation: Int) = Spacing(name.hashCode(), spacing, separation)
        fun template(name: String, structure: String, params: GenParams?, biomes: Condition<ResourceLocation>,
                     types: Condition<BiomeDictionary.Type> = Condition(), locator: List<Step> = listOf(Step.RandomHeight),
                     flowers: List<OrganaFlower> = emptyList()) =
            WorldFeature(id(name), FeatureKind.STRUCTURE, 0, Feature.Template(ResourceLocation(Tags.MODID, structure), locator, flowers),
                params, biomes = biomes, biomeTypes = types)
        fun flower(dx: Int, dz: Int, chance: Int) = OrganaFlower(listOf(Step.Move(dx, 0, dz), Step.Surface(grass)), chance)
        val end = Condition.of(BiomeDictionary.Type.END)
        return ordered(FeatureKind.STRUCTURE, listOf(
            template("end_ruins", "end_ruins", params(85, 100, spacing = grid("end_ruins", 16, 10)), Condition.of(mc("sky")), end),
            template("end_endermite_island", "end_endermite_island", params(90, 115, spacing = grid("end_endermite_island", 64, 50)), islandBiomes),
            template("end_entropy_island", "end_entropy_island", params(90, 115, rarity = 600), islandBiomes),
            // The old data reused the id of the endermite island here, so both had the same position. This one has its own id.
            template("end_moss_island", "end_moss_island", params(90, 115, spacing = grid("end_moss_island", 64, 50)), islandBiomes,
                types = Condition(mapOf(BiomeDictionary.Type.getType("ORGANA") to params(90, 115, rarity = 120))),
                flowers = listOf(flower(7, 7, 100), flower(4, 9, 31), flower(11, 6, 31))),
            template("observatory", "observ", params(3, 3, spacing = grid("observatory", 64, 50)),
                Condition.of(mc("desert"), mc("ocean"), mc("deep_ocean"), mc("forest"), mc("birch_forest"), mc("swampland")),
                locator = listOf(Step.Surface(), Step.Move(0, 1, 0))),
            template("ship_wreck", "end_shipwreck", params(50, 70, spacing = grid("ship_wreck", 64, 50)), Condition.of(mc("sky")), end,
                locator = listOf(Step.Move(10, 0, 10), Step.Surface(), Step.Move(0, -2, 2))),
        ))
    }

    fun endBiomes(): List<EndBiome> = listOf(ModBioms.ORGANA_BIOME, ModBioms.ENTROPY_BIOME)
        .map { EndBiome(it.registryName!!, 1000) }
}
