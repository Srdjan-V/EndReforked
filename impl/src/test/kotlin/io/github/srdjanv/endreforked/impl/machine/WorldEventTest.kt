package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.BlockSpec
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.api.machine.nextWorldEvent
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText

class WorldEventTest {
    private val materializer = Id("endreborn", "materializer")
    private val stone = BlockSpec(Id("minecraft", "stone"), mapOf("variant" to "granite"))
    private val shape = StructureSpec(listOf(listOf("SS", "S "), listOf("S ", "  ")), mapOf('S' to stone))
    private val setBlock = WorldEvent(30, 15, shape, WorldAction.SetBlock(BlockSpec(Id("minecraft", "iron_block"))))
    private val drop = WorldEvent(90, 2, shape, WorldAction.DropItem(ItemOut(Id("minecraft", "diamond"), 0, 2)))

    private val recipe = MachineRecipe(
        Id("test", "m"), materializer,
        listOf(ItemIngredient.Item(Id("minecraft", "diamond")), ItemIngredient.Item(Id("endreborn", "catalyst"))),
        itemOutputs = listOf(ItemOut(Id("minecraft", "diamond"))), ticks = 600, worldEvents = listOf(drop, setBlock),
    )

    private fun registry() = MachineRecipes.managed({ listOf(recipe) })

    @Test
    fun nextEventIsTheFirstAfterThePercent() {
        assertEquals(setBlock, recipe.nextWorldEvent(0))
        assertEquals(drop, recipe.nextWorldEvent(30))
        assertNull(recipe.nextWorldEvent(90))
    }

    @Test
    fun wireRoundTrip() {
        val snapshot = MachineRecipes(listOf(recipe)).toSnapshot()
        val decoded = RegistrySnapshot.ADAPTER.decode(RegistrySnapshot.ADAPTER.encode(snapshot))
        assertEquals(listOf(recipe), decoded.toMachineRecipes())
    }

    @Test
    fun jsonDecodesWorldEvents(@TempDir dir: Path) {
        dir.resolve("m.json").writeText(
            """{"id":"test:m","machine":"endreborn:materializer","ticks":600,
                "itemInputs":[{"item":"minecraft:diamond"},{"item":"endreborn:catalyst"}],
                "itemOutputs":[{"item":"minecraft:diamond"}],
                "worldEvents":[
                  {"atPercent":30,"chance":15,
                   "structure":{"aisles":[["SS","S "],["S ","  "]],"symbols":{"S":{"block":"minecraft:stone","properties":{"variant":"granite"}}}},
                   "action":{"type":"set_block","block":"minecraft:iron_block"}},
                  {"atPercent":90,"chance":2,
                   "structure":{"aisles":[["SS","S "],["S ","  "]],"symbols":{"S":{"block":"minecraft:stone","properties":{"variant":"granite"}}}},
                   "action":{"type":"drop_item","item":"minecraft:diamond","count":2}}]}""",
        )
        val registry = registry()
        val report = registry.reload(dir)
        assertEquals(emptyList<String>(), report.errors)
        assertEquals(listOf(setBlock, drop), registry.current[Id("test", "m")]?.worldEvents)
    }

    private fun bad(event: WorldEvent) = MachineRecipes.validate(recipe.copy(worldEvents = listOf(event)))

    @Test
    fun invalidWorldEventsAreRejected() {
        assertTrue(bad(setBlock.copy(atPercent = 0))!!.contains("atPercent"))
        assertTrue(bad(setBlock.copy(atPercent = 101))!!.contains("atPercent"))
        assertTrue(bad(setBlock.copy(chance = 101))!!.contains("chance"))
        assertTrue(bad(setBlock.copy(structure = shape.copy(aisles = emptyList())))!!.contains("empty"))
        assertTrue(bad(setBlock.copy(structure = shape.copy(aisles = listOf(listOf("SS"), listOf("S")))))!!.contains("differ"))
        assertTrue(bad(setBlock.copy(structure = shape.copy(aisles = listOf(listOf("SX")))))!!.contains("symbol 'X'"))
        assertTrue(bad(setBlock.copy(structure = shape.copy(aisles = listOf(listOf("  ")))))!!.contains("only blanks"))
        assertTrue(bad(setBlock.copy(structure = shape.copy(symbols = mapOf(' ' to stone, 'S' to stone))))!!.contains("blank"))
        assertTrue(bad(drop.copy(action = WorldAction.DropItem(ItemOut(Id("minecraft", "diamond"), 0, 0))))!!.contains("drop"))
        assertTrue(MachineRecipes.validate(recipe.copy(worldEvents = listOf(setBlock, setBlock)))!!.contains("same percent"))
    }

    @Test
    fun badJsonWorldEventsAreReportedAndSkipped(@TempDir dir: Path) {
        val common = """"machine":"endreborn:materializer","ticks":10,"itemInputs":[{"item":"minecraft:diamond"}],"itemOutputs":[{"item":"minecraft:dirt"}]"""
        val structure = """"structure":{"aisles":[["S"]],"symbols":{"S":{"block":"minecraft:stone"}}}"""
        dir.resolve("bad.json").writeText(
            """[{"id":"test:type",$common,"worldEvents":[{"atPercent":5,"chance":5,$structure,"action":{"type":"explode"}}]},
                {"id":"test:both",$common,"worldEvents":[{"atPercent":5,"chance":5,$structure,"action":{"type":"set_block","block":"minecraft:dirt","item":"minecraft:dirt"}}]},
                {"id":"test:symbol",$common,"worldEvents":[{"atPercent":5,"chance":5,"structure":{"aisles":[["S"]],"symbols":{"SS":{"block":"minecraft:stone"}}},"action":{"type":"set_block","block":"minecraft:dirt"}}]},
                {"id":"test:range",$common,"worldEvents":[{"atPercent":500,"chance":5,$structure,"action":{"type":"set_block","block":"minecraft:dirt"}}]},
                {"id":"test:typo",$common,"worldEvents":[{"atPercent":5,"chance":5,$structure,"action":{"type":"set_block","block":"minecraft:dirt"},"extra":1}]}]""",
        )
        val registry = registry()
        val report = registry.reload(dir)
        assertEquals(5, report.errors.size, report.errors.toString())
        assertTrue(report.errors.any { it.startsWith("bad.json: test:type: unknown action type 'explode'") })
        assertTrue(report.errors.any { it.startsWith("bad.json: test:both: set_block has no 'item'") })
        assertTrue(report.errors.any { it.startsWith("bad.json: test:symbol: symbol 'SS'") })
        assertTrue(report.errors.any { it.startsWith("bad.json: test:range: world event at 500%") })
        assertTrue(report.errors.any { it.startsWith("bad.json: test:typo") })
        assertEquals(1, registry.current.size) // only the default
    }
}
