package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.machine.removeByItemInput
import io.github.srdjanv.endreforked.api.machine.removeByItemOutput
import io.github.srdjanv.endreforked.wire.s2c.FluidAmount
import io.github.srdjanv.endreforked.wire.s2c.Ingredient
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import io.github.srdjanv.endreforked.wire.s2c.Stack
import io.github.srdjanv.endreforked.wire.s2c.MachineRecipe as WireRecipe
import io.github.srdjanv.endreforked.wire.s2c.MachineRecipes as WireRecipes
import net.minecraft.init.Blocks
import net.minecraft.init.Bootstrap
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MachineRecipeRegistryTest {
    init {
        Bootstrap.register()
    }

    private fun id(path: String) = ResourceLocation("test", path)
    private val chamber = ResourceLocation("endreborn", "entropy_chamber")
    private val a = id("a")
    private val b = id("b")
    private val endStone = Item.getItemFromBlock(Blocks.END_STONE)

    private val default = MachineRecipe(
        a, chamber, listOf(ItemIngredient.Item(ItemStack(Blocks.END_STONE, 4))),
        itemOutputs = listOf(ItemStack(Blocks.DIRT, 2)), ticks = 100, entropy = 2,
    )
    private val other = default.copy(id = b)

    private fun registry(defaults: List<MachineRecipe> = listOf(default, other)) = MachineRecipes.managed { defaults }

    @Test
    fun theDefaultsAreLoaded() {
        val registry = registry()
        val report = registry.rebuild()
        assertEquals(2, report.total)
        assertEquals(default, registry.current[a])
    }



    @Test
    fun validationRejectsDefaultsToo() {
        val registry = registry(listOf(default, other.copy(ticks = 0)))
        val report = registry.rebuild()
        assertEquals(listOf("code defaults: test:b: ticks must be at least 1"), report.errors)
        assertNull(registry.current[b])
    }

    @Test
    fun snapshotIsReplacedAndListenersAreCalled() {
        val registry = registry()
        registry.rebuild()
        val old = registry.current
        var seen: MachineRecipes? = null
        registry.addListener { seen = it }
        registry.replace(listOf(default))
        assertEquals(2, old.size) // the old snapshot did not change
        assertEquals(1, registry.current.size)
        assertSame(registry.current, seen)
    }

    @Test
    fun wireRoundTrip() {
        val full = default.copy(
            itemInputs = default.itemInputs + ItemIngredient.OreDict("logWood", 2),
            fluidInputs = listOf(FluidStack(FluidRegistry.LAVA, 1000)),
            fluidOutputs = listOf(FluidStack(FluidRegistry.WATER, 500)),
            priority = 3,
        )
        val snapshot = MachineRecipes(listOf(full, other)).toSnapshot()
        val decoded = RegistrySnapshot.ADAPTER.decode(RegistrySnapshot.ADAPTER.encode(snapshot))
        assertEquals("endreborn:machine_recipes", decoded.registry)
        val recipes = decoded.toMachineRecipes()!!
        assertEquals(snapshot, MachineRecipes(recipes).toSnapshot()) // stacks have no equals, so compare the encoding
        val first = recipes[0]
        assertEquals(a, first.id)
        assertEquals(chamber, first.machine)
        assertEquals(endStone, (first.itemInputs[0] as ItemIngredient.Item).stack.item)
        assertEquals(4, first.itemInputs[0].count)
        assertEquals(ItemIngredient.OreDict("logWood", 2), first.itemInputs[1])
        assertSame(FluidRegistry.LAVA, first.fluidInputs[0].fluid)
        assertEquals(500, first.fluidOutputs[0].amount)
        assertEquals(2, first.itemOutputs[0].count)
        assertEquals(3, first.priority)
    }

    @Test
    fun aRecipeWithSomethingThisClientDoesNotHaveIsSkipped() {
        val good = MachineRecipes(listOf(default)).toSnapshot().machine_recipes!!.recipes[0]
        val unknownItem = good.copy(id = "test:item", item_inputs = listOf(Ingredient(item = Stack("nomod:thing", 0, 1))))
        val unknownFluid = good.copy(id = "test:fluid", fluid_inputs = listOf(FluidAmount("nomod_fluid", 1)))
        val snapshot = RegistrySnapshot(registry = "endreborn:machine_recipes", machine_recipes = WireRecipes(listOf(unknownItem, good, unknownFluid)))
        assertEquals(listOf(a), snapshot.toMachineRecipes()!!.map { it.id })
    }

    @Test
    fun scriptsAddedBeforeTheFirstFreezeAreAppliedByIt() {
        val registry = registry()
        registry.add(default.copy(id = id("early")))
        assertTrue(!registry.isFrozen)
        assertEquals(0, registry.current.size)
        registry.rebuild()
        assertTrue(registry.isFrozen)
        assertNotNull(registry.current[id("early")])
    }

    @Test
    fun scriptsAddedAfterTheFreezeNeedAnExplicitRebuild() {
        val registry = registry()
        registry.rebuild()
        registry.add(default.copy(id = id("late")))
        registry.remove(a)
        assertNull(registry.current[id("late")]) // the snapshot did not change yet
        assertNotNull(registry.current[a])
        val report = registry.rebuild()
        assertNotNull(registry.current[id("late")])
        assertNull(registry.current[a])
        assertEquals(2, report.applied["scripts"])
    }


    @Test
    fun scriptRemovesADefaultByPredicate() {
        val registry = registry()
        registry.removeByItemInput(endStone)
        val report = registry.rebuild()
        assertEquals(0, registry.current.size)
        assertEquals(2, report.applied["scripts"])
        registry.clear()
        registry.removeByItemOutput(Items.APPLE)
        registry.rebuild()
        assertEquals(2, registry.current.size)
    }

    @Test
    fun scriptPredicateSeesTheResultOfEarlierLayers() {
        val registry = registry()
        registry.add(default.copy(id = id("scripted")))
        registry.removeIf { it.id.namespace == "test" }
        registry.add(default.copy(id = id("after")))
        registry.rebuild()
        assertEquals(listOf(id("after")), registry.current.entries.map { it.id })
    }

    @Test
    fun clearingTheScriptLayerRestoresThePreviousState() {
        val registry = registry()
        registry.rebuild()
        val before = registry.current.entries.toList()
        registry.add(default.copy(id = id("x")))
        registry.remove(a)
        registry.rebuild()
        assertEquals(2, registry.current.size)
        registry.clear()
        registry.rebuild()
        assertEquals(before, registry.current.entries.toList())
    }


    @Test
    fun invalidScriptedEntryIsReportedAndSkipped() {
        val registry = registry()
        registry.add(default.copy(id = id("bad"), ticks = 0))
        val report = registry.rebuild()
        assertEquals(listOf("scripts: test:bad: ticks must be at least 1"), report.errors)
        assertNull(registry.current[id("bad")])
    }

    @Test
    fun everyRebuildCallsTheListeners() {
        val registry = registry()
        val seen = ArrayList<Int>()
        registry.addListener { seen += it.size }
        registry.rebuild()
        registry.add(default.copy(id = id("late")))
        registry.rebuild()
        assertEquals(listOf(2, 3), seen)
    }
}
