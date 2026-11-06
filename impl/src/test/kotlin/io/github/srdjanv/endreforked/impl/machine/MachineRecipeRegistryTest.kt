package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.removeByItemInput
import io.github.srdjanv.endreforked.api.machine.removeByItemOutput
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.registry.Id
import io.github.srdjanv.endreforked.wire.s2c.RegistrySnapshot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MachineRecipeRegistryTest {
    private val chamber = Id("endreborn", "entropy_chamber")
    private val a = Id("test", "a")
    private val b = Id("test", "b")

    private val default = MachineRecipe(
        a, chamber, listOf(ItemIngredient.Item(Id("minecraft", "end_stone"), 0, 4)),
        itemOutputs = listOf(ItemOut(Id("minecraft", "dirt"), 0, 2)), ticks = 100, entropy = 2,
    )
    private val other = default.copy(id = b)

    private fun registry(extra: (MachineRecipe) -> String? = { null }) =
        MachineRecipes.managed({ listOf(default, other) }, extra)

    @Test
    fun theDefaultsAreLoaded() {
        val registry = registry()
        val report = registry.rebuild()
        assertEquals(2, report.total)
        assertEquals(default, registry.current[a])
    }



    @Test
    fun extraValidationRejectsDefaultsToo() {
        val registry = registry { if (it.id == b) "unknown item" else null }
        val report = registry.rebuild()
        assertEquals(listOf("code defaults: test:b: unknown item"), report.errors)
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
            fluidInputs = listOf(io.github.srdjanv.endreforked.api.machine.FluidIngredient(Id("minecraft", "lava"), 1000)),
            fluidOutputs = listOf(io.github.srdjanv.endreforked.api.machine.FluidOut(Id("minecraft", "water"), 500)),
            priority = 3,
        )
        val snapshot = MachineRecipes(listOf(full, other)).toSnapshot()
        val decoded = RegistrySnapshot.ADAPTER.decode(RegistrySnapshot.ADAPTER.encode(snapshot))
        assertEquals("endreborn:machine_recipes", decoded.registry)
        assertEquals(listOf(full, other), decoded.toMachineRecipes())
    }

    @Test
    fun scriptsAddedBeforeTheFirstFreezeAreAppliedByIt() {
        val registry = registry()
        registry.add(default.copy(id = Id("test", "early")))
        assertTrue(!registry.isFrozen)
        assertEquals(0, registry.current.size)
        registry.rebuild()
        assertTrue(registry.isFrozen)
        assertNotNull(registry.current[Id("test", "early")])
    }

    @Test
    fun scriptsAddedAfterTheFreezeNeedAnExplicitRebuild() {
        val registry = registry()
        registry.rebuild()
        registry.add(default.copy(id = Id("test", "late")))
        registry.remove(a)
        assertNull(registry.current[Id("test", "late")]) // the snapshot did not change yet
        assertNotNull(registry.current[a])
        val report = registry.rebuild()
        assertNotNull(registry.current[Id("test", "late")])
        assertNull(registry.current[a])
        assertEquals(2, report.applied["scripts"])
    }


    @Test
    fun scriptRemovesADefaultByPredicate() {
        val registry = registry()
        registry.removeByItemInput(Id("minecraft", "end_stone"))
        val report = registry.rebuild()
        assertEquals(0, registry.current.size)
        assertEquals(2, report.applied["scripts"])
        registry.clear()
        registry.removeByItemOutput(Id("minecraft", "nothing"))
        registry.rebuild()
        assertEquals(2, registry.current.size)
    }

    @Test
    fun scriptPredicateSeesTheResultOfEarlierLayers() {
        val registry = registry()
        registry.add(default.copy(id = Id("test", "scripted")))
        registry.removeIf { it.id.namespace == "test" }
        registry.add(default.copy(id = Id("test", "after")))
        registry.rebuild()
        assertEquals(listOf(Id("test", "after")), registry.current.entries.map { it.id })
    }

    @Test
    fun clearingTheScriptLayerRestoresThePreviousState() {
        val registry = registry()
        registry.rebuild()
        val before = registry.current.entries.toList()
        registry.add(default.copy(id = Id("test", "x")))
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
        registry.add(default.copy(id = Id("test", "bad"), ticks = 0))
        val report = registry.rebuild()
        assertEquals(listOf("scripts: test:bad: ticks must be at least 1"), report.errors)
        assertNull(registry.current[Id("test", "bad")])
    }

    @Test
    fun everyRebuildCallsTheListeners() {
        val registry = registry()
        val seen = ArrayList<Int>()
        registry.addListener { seen += it.size }
        registry.rebuild()
        registry.add(default.copy(id = Id("test", "late")))
        registry.rebuild()
        assertEquals(listOf(2, 3), seen)
    }
}
