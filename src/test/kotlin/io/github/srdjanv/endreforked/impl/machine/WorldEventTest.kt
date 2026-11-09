package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import io.github.srdjanv.endreforked.api.machine.nextWorldEvent
import io.github.srdjanv.endreforked.common.materializer.WorldEvents
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import net.minecraft.block.BlockStone
import net.minecraft.init.Blocks
import net.minecraft.init.Bootstrap
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WorldEventTest {
    init {
        Bootstrap.register()
    }

    private val materializer = ResourceLocation("endreborn", "materializer")
    private val stone = Blocks.STONE.defaultState.withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE)
    private val shape = StructureSpec(listOf(listOf("SS", "S "), listOf("S ", "  ")), mapOf('S' to stone))
    private val setBlock = WorldEvent(30, 15, shape, WorldAction.SetBlock(Blocks.IRON_BLOCK.defaultState))
    private val drop = WorldEvent(90, 2, shape, WorldAction.DropItem(ItemStack(Items.DIAMOND, 2)))

    private val recipe = MachineRecipe(
        ResourceLocation("test", "m"), materializer,
        listOf(ItemIngredient.Item(ItemStack(Items.DIAMOND)), ItemIngredient.Item(ItemStack(Items.EMERALD))),
        itemOutputs = listOf(ItemStack(Items.DIAMOND)), ticks = 600, worldEvents = listOf(drop, setBlock),
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
        val event = decoded.toMachineRecipes()!!.single().worldEvents
        assertEquals(snapshot, MachineRecipes(decoded.toMachineRecipes()!!).toSnapshot()) // stacks have no equals, so compare the encoding
        assertEquals(mapOf('S' to stone), event[1].structure.symbols) // the block state with its property came back
        assertEquals(listOf(listOf("SS", "S "), listOf("S ", "  ")), event[1].structure.aisles)
        assertEquals(Blocks.IRON_BLOCK.defaultState, (event[1].action as WorldAction.SetBlock).block)
        assertEquals(2, (event[0].action as WorldAction.DropItem).item.count)
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
        assertTrue(bad(drop.copy(action = WorldAction.DropItem(ItemStack(Items.DIAMOND, 0))))!!.contains("dropped item"))
        assertTrue(MachineRecipes.validate(recipe.copy(worldEvents = listOf(setBlock, setBlock)))!!.contains("same percent"))
    }

    @Test
    fun badScriptedWorldEventIsReportedAndSkipped() {
        val registry = registry()
        registry.add(recipe.copy(id = ResourceLocation("test", "range"), worldEvents = listOf(setBlock.copy(atPercent = 500))))
        registry.add(recipe.copy(id = ResourceLocation("test", "good"), worldEvents = listOf(setBlock)))
        val report = registry.rebuild()
        assertEquals(1, report.errors.size, report.errors.toString())
        assertTrue(report.errors.single().startsWith("scripts: test:range: world event at 500%"))
        assertEquals(2, registry.current.size) // the default and test:good
    }

    @Test
    fun theSearchAreaIs7x7AndFiveHighWithoutTheMachineColumn() {
        assertTrue(WorldEvents.inArea(3, 0, -3))
        assertTrue(WorldEvents.inArea(-3, 5, 3))
        assertTrue(!WorldEvents.inArea(4, 0, 0))
        assertTrue(!WorldEvents.inArea(0, 0, -4))
        assertTrue(!WorldEvents.inArea(1, -1, 1)) // below the machine
        assertTrue(!WorldEvents.inArea(1, 6, 1))
        assertTrue(!WorldEvents.inArea(0, 3, 0)) // the column of the machine
    }

    @Test
    fun theGhostStartsAboveAndInFrontOfTheMachine() {
        val machine = BlockPos(10, 64, 10)
        assertEquals(BlockPos(10, 65, 9), WorldEvents.ghostPos(machine, EnumFacing.NORTH, 0, 0, 0))
        // aisle = up, row = away from the machine, column = to the right of the facing (east for north)
        assertEquals(BlockPos(12, 66, 7), WorldEvents.ghostPos(machine, EnumFacing.NORTH, 1, 2, 2))
        assertEquals(BlockPos(9, 65, 12), WorldEvents.ghostPos(machine, EnumFacing.SOUTH, 0, 1, 1)) // west is the right of south
    }
}
