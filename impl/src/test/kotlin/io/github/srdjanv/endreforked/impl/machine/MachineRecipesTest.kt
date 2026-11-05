package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.FluidIngredient
import io.github.srdjanv.endreforked.api.machine.FluidOut
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.ItemOut
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.api.registry.Id
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MachineRecipesTest {
    private val chamber = Id("endreborn", "entropy_chamber")
    private val endStone = Id("minecraft", "end_stone")
    private val lava = Id("minecraft", "lava")
    private val water = Id("minecraft", "water")

    private fun recipe(
        path: String,
        items: List<ItemIngredient> = emptyList(),
        fluids: List<FluidIngredient> = emptyList(),
        priority: Int = 0,
    ) = MachineRecipe(
        Id("test", path), chamber, items, fluids,
        itemOutputs = listOf(ItemOut(Id("test", "out"))), ticks = 10, priority = priority,
    )

    private fun stone(meta: Int, count: Int) = ItemInput(ItemKey(endStone, meta), count)

    @Test
    fun exactMatch() {
        val r = recipe("a", listOf(ItemIngredient.Item(endStone, 0, 4)))
        val match = MachineRecipes(listOf(r)).find(chamber, listOf(stone(0, 4)), emptyList())
        assertEquals(r, match?.recipe)
        assertArrayEquals(intArrayOf(4), match?.itemsTaken)
    }

    @Test
    fun insufficientCountDoesNotMatch() {
        val recipes = MachineRecipes(listOf(recipe("a", listOf(ItemIngredient.Item(endStone, 0, 4)))))
        assertNull(recipes.find(chamber, listOf(stone(0, 3)), emptyList()))
    }

    @Test
    fun extraItemsAreAllowedAndOnlyTheNeededCountIsTaken() {
        val recipes = MachineRecipes(listOf(recipe("a", listOf(ItemIngredient.Item(endStone, 0, 4)))))
        val match = recipes.find(chamber, listOf(stone(0, 10)), emptyList())
        assertArrayEquals(intArrayOf(4), match?.itemsTaken)
    }

    @Test
    fun countIsSummedOverSlots() {
        val recipes = MachineRecipes(listOf(recipe("a", listOf(ItemIngredient.Item(endStone, 0, 4)))))
        val match = recipes.find(chamber, listOf(stone(0, 3), null, stone(0, 3)), emptyList())
        assertArrayEquals(intArrayOf(3, 0, 1), match?.itemsTaken)
    }

    @Test
    fun wrongMetaDoesNotMatchButWildcardDoes() {
        val exact = MachineRecipes(listOf(recipe("a", listOf(ItemIngredient.Item(endStone, 1, 1)))))
        assertNull(exact.find(chamber, listOf(stone(2, 1)), emptyList()))
        val wild = MachineRecipes(listOf(recipe("a", listOf(ItemIngredient.Item(endStone, count = 1)))))
        assertNotNull(wild.find(chamber, listOf(stone(7, 1)), emptyList()))
    }

    @Test
    fun otherMachineDoesNotMatch() {
        val recipes = MachineRecipes(listOf(recipe("a", listOf(ItemIngredient.Item(endStone, 0, 1)))))
        assertNull(recipes.find(Id("endreborn", "other"), listOf(stone(0, 1)), emptyList()))
    }

    @Test
    fun oreDict() {
        val recipes = MachineRecipes(listOf(recipe("a", listOf(ItemIngredient.OreDict("logWood", 2)))))
        val ores = OreResolver { key, ore -> ore == "logWood" && key.id.path.endsWith("log") }
        val log = ItemInput(ItemKey(Id("minecraft", "log"), 0), 2)
        assertNotNull(recipes.find(chamber, listOf(log), emptyList(), ores))
        assertNull(recipes.find(chamber, listOf(log), emptyList())) // no resolver, no match
        assertNull(recipes.find(chamber, listOf(stone(0, 2)), emptyList(), ores))
        assertTrue(recipes.acceptsItem(chamber, ItemKey(Id("minecraft", "log"), 3), ores))
    }

    @Test
    fun fluidAmountIsRespected() {
        val recipes = MachineRecipes(listOf(recipe("a", fluids = listOf(FluidIngredient(lava, 2000)))))
        assertNull(recipes.find(chamber, emptyList(), listOf(FluidInput(lava, 1999))))
        assertNull(recipes.find(chamber, emptyList(), listOf(FluidInput(water, 5000))))
        val match = recipes.find(chamber, emptyList(), listOf(FluidInput(lava, 5000)))
        assertArrayEquals(intArrayOf(2000), match?.fluidsTaken)
    }

    @Test
    fun mixedItemAndFluid() {
        val r = recipe("a", listOf(ItemIngredient.Item(endStone, 0, 1)), listOf(FluidIngredient(lava, 100)))
        val recipes = MachineRecipes(listOf(r))
        assertEquals(r, recipes.find(chamber, listOf(stone(0, 1)), listOf(FluidInput(lava, 100)))?.recipe)
        assertNull(recipes.find(chamber, listOf(stone(0, 1)), emptyList())) // an item-only pipeline never finds it
        assertNull(recipes.find(chamber, emptyList(), listOf(FluidInput(lava, 100))))
    }

    @Test
    fun priorityThenIdDecideBetweenMatches() {
        val items = listOf(ItemIngredient.Item(endStone, 0, 1))
        val recipes = MachineRecipes(listOf(
            recipe("b", items), recipe("a", items), recipe("c", items, priority = 5), recipe("d", items, priority = 5),
        ))
        assertEquals(Id("test", "c"), recipes.find(chamber, listOf(stone(0, 1)), emptyList())?.recipe?.id)
        val equal = MachineRecipes(listOf(recipe("b", items), recipe("a", items)))
        assertEquals(Id("test", "a"), equal.find(chamber, listOf(stone(0, 1)), emptyList())?.recipe?.id)
    }

    @Test
    fun specificIngredientIsTakenBeforeOre() {
        val log = ItemKey(Id("minecraft", "log"), 0)
        val recipes = MachineRecipes(listOf(
            recipe("a", listOf(ItemIngredient.OreDict("logWood", 1), ItemIngredient.Item(log.id, 0, 1))),
        ))
        val ores = OreResolver { _, ore -> ore == "logWood" }
        // slot 0 is the only log. The specific ingredient takes it, so the ore ingredient needs slot 1
        val inputs = listOf(ItemInput(log, 1), ItemInput(ItemKey(Id("minecraft", "plank_log"), 0), 1))
        val match = recipes.find(chamber, inputs, emptyList(), ores)
        assertArrayEquals(intArrayOf(1, 1), match?.itemsTaken)
    }

    @Test
    fun acceptsOnlyUsefulInput() {
        val recipes = MachineRecipes(listOf(
            recipe("a", listOf(ItemIngredient.Item(endStone, 0, 4))),
            recipe("b", fluids = listOf(FluidIngredient(lava, 1))),
        ))
        assertTrue(recipes.acceptsItem(chamber, ItemKey(endStone, 0), OreResolver.NONE))
        assertTrue(!recipes.acceptsItem(chamber, ItemKey(endStone, 1), OreResolver.NONE))
        assertTrue(recipes.acceptsFluid(chamber, lava))
        assertTrue(!recipes.acceptsFluid(chamber, water))
    }

    @Test
    fun validationRejectsBrokenRecipes() {
        val ok = recipe("a", listOf(ItemIngredient.Item(endStone, 0, 1)))
        assertNull(MachineRecipes.validate(ok))
        assertNotNull(MachineRecipes.validate(ok.copy(itemInputs = emptyList())))
        assertNotNull(MachineRecipes.validate(ok.copy(itemOutputs = emptyList(), fluidOutputs = emptyList())))
        assertNotNull(MachineRecipes.validate(ok.copy(ticks = 0)))
        assertNotNull(MachineRecipes.validate(ok.copy(itemInputs = listOf(ItemIngredient.Item(endStone, 0, 0)))))
        assertNotNull(MachineRecipes.validate(ok.copy(fluidOutputs = listOf(FluidOut(water, 0)))))
    }
}
