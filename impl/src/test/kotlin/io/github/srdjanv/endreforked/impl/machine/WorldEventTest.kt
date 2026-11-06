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
    fun badScriptedWorldEventIsReportedAndSkipped() {
        val registry = registry()
        registry.add(recipe.copy(id = Id("test", "range"), worldEvents = listOf(setBlock.copy(atPercent = 500))))
        registry.add(recipe.copy(id = Id("test", "good"), worldEvents = listOf(setBlock)))
        val report = registry.rebuild()
        assertEquals(1, report.errors.size, report.errors.toString())
        assertTrue(report.errors.single().startsWith("scripts: test:range: world event at 500%"))
        assertEquals(2, registry.current.size) // the default and test:good
    }
}
