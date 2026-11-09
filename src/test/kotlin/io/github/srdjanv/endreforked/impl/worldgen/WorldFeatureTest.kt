package io.github.srdjanv.endreforked.impl.worldgen

import io.github.srdjanv.endreforked.api.worldgen.Condition
import io.github.srdjanv.endreforked.api.worldgen.EndBiome
import io.github.srdjanv.endreforked.api.worldgen.Feature
import io.github.srdjanv.endreforked.api.worldgen.FeatureKind
import io.github.srdjanv.endreforked.api.worldgen.GenParams
import io.github.srdjanv.endreforked.api.worldgen.Spacing
import io.github.srdjanv.endreforked.api.worldgen.WorldFeature
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.BiomeDictionary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Random

class WorldFeatureTest {
    // Template needs no block state, so these tests run without a Minecraft bootstrap.
    private val feature = Feature.Template(ResourceLocation("test:structure"))
    private fun id(path: String) = ResourceLocation("test", path)
    private fun loc(path: String) = ResourceLocation("minecraft", path)
    private fun type(name: String) = BiomeDictionary.Type.getType(name)
    private fun params(rarity: Int = 0, amount: Int = 0) = GenParams(0, 64, rarity = rarity, amount = amount)

    private val sky = loc("sky")
    private val plains = loc("plains")
    private val end = type("END")
    private val organa = type("ORGANA")
    private val magical = type("MAGICAL")

    private fun wf(
        name: String = "a", weight: Int = 0, kind: FeatureKind = FeatureKind.ORE, params: GenParams? = params(),
        dims: Condition<Int> = Condition(), biomes: Condition<ResourceLocation> = Condition(),
        types: Condition<BiomeDictionary.Type> = Condition(),
    ) = WorldFeature(id(name), kind, weight, feature, params, dims, biomes, types)

    @Test
    fun aFeatureWithoutWhitelistsGeneratesEverywhere() {
        val f = wf()
        assertTrue(f.allowsDimension(0) && f.allowsDimension(-1) && f.allowsDimension(1))
        assertTrue(f.allowsBiome(sky, listOf(end)))
        assertTrue(f.allowsBiome(plains, emptyList()))
    }

    @Test
    fun aDimensionWhitelistKeepsOtherDimensionsOut() {
        val f = wf(dims = Condition.of(1))
        assertTrue(f.allowsDimension(1))
        assertFalse(f.allowsDimension(0))
    }

    @Test
    fun aBiomeOrABiomeTypeInTheWhitelistsLetsTheBiomePass() {
        val f = wf(biomes = Condition.of(sky), types = Condition.of(organa))
        assertTrue(f.allowsBiome(sky, emptyList()))
        assertTrue(f.allowsBiome(loc("organa"), listOf(end, organa)))
        assertFalse(f.allowsBiome(plains, listOf(end)))
    }

    @Test
    fun aTypeWhitelistAloneStillFiltersBiomes() {
        val f = wf(types = Condition.of(end))
        assertTrue(f.allowsBiome(sky, listOf(end)))
        assertFalse(f.allowsBiome(plains, listOf(magical)))
        assertFalse(f.allowsBiome(plains, emptyList()))
    }

    @Test
    fun aDenyAlwaysWins() {
        val f = wf(
            dims = Condition(deny = setOf(-1)),
            biomes = Condition(allow = mapOf(sky to null), deny = setOf(plains)),
            types = Condition(deny = setOf(magical)),
        )
        assertFalse(f.allowsDimension(-1))
        assertTrue(f.allowsDimension(1))
        assertFalse(f.allowsBiome(plains, emptyList()))
        // The biome is on the whitelist, but its type is denied.
        assertFalse(f.allowsBiome(sky, listOf(magical)))
        assertTrue(f.allowsBiome(sky, listOf(end)))
    }

    @Test
    fun aDenyOnlyFeatureStillGeneratesInOtherBiomes() {
        val f = wf(biomes = Condition(deny = setOf(plains)), types = Condition(deny = setOf(magical)))
        assertTrue(f.allowsBiome(sky, listOf(end)))
        assertFalse(f.allowsBiome(plains, listOf(end)))
        assertFalse(f.allowsBiome(sky, listOf(magical)))
    }

    @Test
    fun paramsAreTakenFromBiomeThenTypeThenDimensionThenDefault() {
        val byBiome = params(1)
        val byType = params(2)
        val byDim = params(3)
        val byDefault = params(4)
        val f = wf(
            params = byDefault,
            dims = Condition(mapOf(0 to byDim, 1 to byDim)),
            biomes = Condition(mapOf(sky to byBiome)),
            types = Condition(mapOf(organa to byType)),
        )
        assertEquals(byBiome, f.paramsAt(1, sky, listOf(end, organa)))
        assertEquals(byType, f.paramsAt(1, loc("organa"), listOf(end, organa)))
        // A biome that is allowed but has no own params of its biome or type falls back to the dimension, then to the default.
        val g = f.copy(biomes = Condition.of(plains))
        assertEquals(byDim, g.paramsAt(0, plains, listOf(end)))
        assertEquals(byDefault, g.copy(dimensions = Condition()).paramsAt(5, plains, listOf(end)))
    }

    @Test
    fun dimensionParamsApplyWhenNothingMoreSpecificFits() {
        val obsidian = wf(
            "obsidian", params = null,
            dims = Condition(mapOf(0 to params(rarity = 80, amount = 20), 1 to params(rarity = 60, amount = 40))),
        )
        assertEquals(80, obsidian.paramsAt(0, plains, emptyList())?.rarity)
        assertEquals(40, obsidian.paramsAt(1, sky, listOf(end))?.amount)
        assertNull(obsidian.paramsAt(-1, plains, emptyList()))
    }

    @Test
    fun theDefaultParamsApplyWhenAKeyHasNone() {
        // tungsten_end: the dimension and the biome are whitelisted without own params
        val f = wf(params = params(5, 25), dims = Condition.of(1), biomes = Condition.of(sky))
        assertEquals(params(5, 25), f.paramsAt(1, sky, listOf(end)))
        assertNull(f.paramsAt(1, loc("organa"), listOf(end)))
        assertNull(f.paramsAt(0, sky, listOf(end)))
    }

    @Test
    fun withoutAnyParamsTheFeatureDoesNotRun() {
        assertNull(wf(params = null, dims = Condition.of(1)).paramsAt(1, sky, emptyList()))
    }

    @Test
    fun ofSeveralTypesTheFirstOfTheConditionWins() {
        val a = params(1)
        val b = params(2)
        val f = wf(types = Condition(linkedMapOf(organa to a, end to b)))
        // The order of the biome's own types does not matter.
        assertEquals(a, f.paramsAt(1, sky, listOf(end, organa)))
        assertEquals(a, f.paramsAt(1, sky, listOf(organa, end)))
        assertEquals(b, f.paramsAt(1, sky, listOf(end)))
    }

    @Test
    fun aTypeWithoutOwnParamsIsSkipped() {
        val b = params(2)
        val f = wf(params = params(9), types = Condition(linkedMapOf(organa to null, end to b)))
        assertEquals(b, f.paramsAt(1, sky, listOf(organa, end)))
        assertEquals(params(9), f.paramsAt(1, sky, listOf(organa)))
    }

    @Test
    fun equalWeightsKeepAllFeaturesAndOrderThemById() {
        val registry = WorldFeatures(listOf(wf("c", 5), wf("a", 5), wf("b", 5), wf("z", 9)))
        assertEquals(4, registry.of(FeatureKind.ORE).size)
        assertEquals(listOf("z", "a", "b", "c"), registry.of(FeatureKind.ORE).map { it.id.path })
    }

    @Test
    fun featuresAreGroupedByKind() {
        val registry = WorldFeatures(listOf(
            wf("s", 1, FeatureKind.STRUCTURE), wf("o", 1, FeatureKind.ORE), wf("g", 1, FeatureKind.GENERIC), wf("o2", 2, FeatureKind.ORE),
        ))
        assertEquals(listOf("o2", "o"), registry.of(FeatureKind.ORE).map { it.id.path })
        assertEquals(listOf("g"), registry.of(FeatureKind.GENERIC).map { it.id.path })
        assertEquals(listOf("s"), registry.of(FeatureKind.STRUCTURE).map { it.id.path })
    }

    @Test
    fun theValidationChecksEveryParams() {
        assertNull(WorldFeatures.validate(wf()))
        assertEquals("has no params", WorldFeatures.validate(wf(params = null)))
        assertNotNull(WorldFeatures.validate(wf(params = GenParams(10, 5))))
        assertNotNull(WorldFeatures.validate(wf(params = params(rarity = -1))))
        assertNotNull(WorldFeatures.validate(wf(params = GenParams(0, 5, spacing = Spacing(1, 10, 10)))))
        assertNull(WorldFeatures.validate(wf(params = GenParams(0, 5, spacing = Spacing(1, 16, 10)))))
        // The override is checked too.
        assertNotNull(WorldFeatures.validate(wf(dims = Condition(mapOf(1 to GenParams(9, 1))))))
        assertNotNull(WorldFeatures.validate(wf().copy(feature = Feature.Template(ResourceLocation("t:x"), locator = emptyList()))))
    }

    @Test
    fun scriptsCanReplaceAndRemoveFeaturesAndBiomes() {
        val registry = WorldFeatures.managed({ listOf(wf("a"), wf("b")) })
        val report = registry.rebuild()
        assertEquals(2, report.total)
        registry.add(wf("a", weight = 7))
        registry.remove(id("b"))
        registry.rebuild()
        assertEquals(listOf(7), registry.current.of(FeatureKind.ORE).map { it.weight })
        assertEquals(1, registry.current.size)

        val biomes = EndBiomes.managed({ listOf(EndBiome(id("x"), 1000), EndBiome(id("y"), 0)) })
        val r = biomes.rebuild()
        assertEquals(1, r.total)
        assertEquals(1, r.errors.size)
    }

    @Test
    fun theStructureGridPicksOneChunkPerCell() {
        val spacing = Spacing(12345, 16, 10)
        val seeded = { x: Int, z: Int, salt: Int -> Random(x * 341873128712L + z * 132897987541L + salt) }
        for (cellX in -3..2) for (cellZ in -3..2) {
            val hits = ArrayList<Pair<Int, Int>>()
            for (x in cellX * 16 until cellX * 16 + 16) for (z in cellZ * 16 until cellZ * 16 + 16) {
                if (spacing.isStructureChunk(x, z, seeded)) hits += x to z
            }
            assertEquals(1, hits.size, "cell $cellX,$cellZ")
            // The offset is at most spacing - separation - 1 chunks into the cell.
            assertTrue(hits[0].first - cellX * 16 in 0 until 6 && hits[0].second - cellZ * 16 in 0 until 6)
        }
    }

    @Test
    fun theUniqueIdChangesThePosition() {
        val seeded = { x: Int, z: Int, salt: Int -> Random(x * 341873128712L + z * 132897987541L + salt) }
        val cells = (-10..10).flatMap { x -> (-10..10).map { z -> x to z } }
        fun chosen(id: Int) = cells.map { (x, z) ->
            (0 until 16).flatMap { dx -> (0 until 16).map { dz -> dx to dz } }
                .first { (dx, dz) -> Spacing(id, 16, 10).isStructureChunk(x * 16 + dx, z * 16 + dz, seeded) }
        }
        assertTrue(chosen(1) != chosen(2))
    }

    @Test
    fun aGeodeHasAShellALiningAndAHollowCentre() {
        // radius 5: the shell is farther than 4 blocks from the centre, the lining farther than 3, the rest is air
        assertEquals(GeodeLayer.AIR, geodeLayer(0.0, 5))
        assertEquals(GeodeLayer.AIR, geodeLayer(3.0, 5))
        assertEquals(GeodeLayer.LINING, geodeLayer(3.01, 5))
        assertEquals(GeodeLayer.LINING, geodeLayer(4.0, 5))
        assertEquals(GeodeLayer.SHELL, geodeLayer(4.01, 5))
        assertEquals(GeodeLayer.SHELL, geodeLayer(5.0, 5))
        // the layers keep their width for any radius
        assertEquals(GeodeLayer.AIR, geodeLayer(5.0, 8))
        assertEquals(GeodeLayer.SHELL, geodeLayer(7.5, 8))
    }
}
