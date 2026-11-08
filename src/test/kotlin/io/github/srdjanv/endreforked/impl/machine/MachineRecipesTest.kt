package io.github.srdjanv.endreforked.impl.machine

import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import net.minecraft.init.Blocks
import net.minecraft.init.Bootstrap
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.oredict.OreDictionary
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MachineRecipesTest {
    init {
        Bootstrap.register() // real items, fluids and the ore dictionary. About a second, once per JVM.
    }

    private val chamber = ResourceLocation("endreborn", "entropy_chamber")
    private val lava = FluidRegistry.LAVA
    private val water = FluidRegistry.WATER

    private fun recipe(
        path: String,
        items: List<ItemIngredient> = emptyList(),
        fluids: List<FluidStack> = emptyList(),
        priority: Int = 0,
    ) = MachineRecipe(
        ResourceLocation("test", path), chamber, items, fluids,
        itemOutputs = listOf(ItemStack(Blocks.DIRT)), ticks = 10, priority = priority,
    )

    private fun endStone(count: Int = 1, meta: Int = 0) = ItemStack(Blocks.END_STONE, count, meta)
    private fun need(count: Int, meta: Int = 0) = ItemIngredient.Item(endStone(count, meta))
    private val empty = ItemStack.EMPTY

    @Test
    fun exactMatch() {
        val r = recipe("a", listOf(need(4)))
        val match = MachineRecipes(listOf(r)).find(chamber, listOf(endStone(4)), emptyList())
        assertEquals(r, match?.recipe)
        assertArrayEquals(intArrayOf(4), match?.itemsTaken)
    }

    @Test
    fun insufficientCountDoesNotMatch() {
        val recipes = MachineRecipes(listOf(recipe("a", listOf(need(4)))))
        assertNull(recipes.find(chamber, listOf(endStone(3)), emptyList()))
    }

    @Test
    fun extraItemsAreAllowedAndOnlyTheNeededCountIsTaken() {
        val recipes = MachineRecipes(listOf(recipe("a", listOf(need(4)))))
        val match = recipes.find(chamber, listOf(endStone(10)), emptyList())
        assertArrayEquals(intArrayOf(4), match?.itemsTaken)
    }

    @Test
    fun countIsSummedOverSlots() {
        val recipes = MachineRecipes(listOf(recipe("a", listOf(need(4)))))
        val match = recipes.find(chamber, listOf(endStone(3), empty, endStone(3)), emptyList())
        assertArrayEquals(intArrayOf(3, 0, 1), match?.itemsTaken)
    }

    @Test
    fun wrongMetaDoesNotMatchButWildcardDoes() {
        val exact = MachineRecipes(listOf(recipe("a", listOf(need(1, meta = 1)))))
        assertNull(exact.find(chamber, listOf(endStone(1, meta = 2)), emptyList()))
        val wild = MachineRecipes(listOf(recipe("a", listOf(need(1, meta = OreDictionary.WILDCARD_VALUE)))))
        assertNotNull(wild.find(chamber, listOf(endStone(1, meta = 7)), emptyList()))
    }

    @Test
    fun otherMachineDoesNotMatch() {
        val recipes = MachineRecipes(listOf(recipe("a", listOf(need(1)))))
        assertNull(recipes.find(ResourceLocation("endreborn", "other"), listOf(endStone()), emptyList()))
    }

    @Test
    fun oreDict() {
        val recipes = MachineRecipes(listOf(recipe("a", listOf(ItemIngredient.OreDict("logWood", 2)))))
        val log = ItemStack(Blocks.LOG, 2)
        assertNotNull(recipes.find(chamber, listOf(log), emptyList()))
        assertNull(recipes.find(chamber, listOf(endStone(2)), emptyList()))
        assertNull(recipes.find(chamber, listOf(ItemStack(Blocks.LOG, 1)), emptyList()))
        assertTrue(recipes.acceptsItem(chamber, ItemStack(Blocks.LOG, 1, 3)))
        assertTrue(!recipes.acceptsItem(chamber, endStone()))
    }

    @Test
    fun fluidAmountIsRespected() {
        val recipes = MachineRecipes(listOf(recipe("a", fluids = listOf(FluidStack(lava, 2000)))))
        assertNull(recipes.find(chamber, emptyList(), listOf(FluidStack(lava, 1999))))
        assertNull(recipes.find(chamber, emptyList(), listOf(FluidStack(water, 5000))))
        val match = recipes.find(chamber, emptyList(), listOf(FluidStack(lava, 5000)))
        assertArrayEquals(intArrayOf(2000), match?.fluidsTaken)
    }

    @Test
    fun mixedItemAndFluid() {
        val r = recipe("a", listOf(need(1)), listOf(FluidStack(lava, 100)))
        val recipes = MachineRecipes(listOf(r))
        assertEquals(r, recipes.find(chamber, listOf(endStone()), listOf(FluidStack(lava, 100)))?.recipe)
        assertNull(recipes.find(chamber, listOf(endStone()), emptyList())) // an item-only pipeline never finds it
        assertNull(recipes.find(chamber, emptyList(), listOf(FluidStack(lava, 100))))
    }

    @Test
    fun priorityThenIdDecideBetweenMatches() {
        val items = listOf(need(1))
        val recipes = MachineRecipes(listOf(
            recipe("b", items), recipe("a", items), recipe("c", items, priority = 5), recipe("d", items, priority = 5),
        ))
        assertEquals(ResourceLocation("test", "c"), recipes.find(chamber, listOf(endStone()), emptyList())?.recipe?.id)
        val equal = MachineRecipes(listOf(recipe("b", items), recipe("a", items)))
        assertEquals(ResourceLocation("test", "a"), equal.find(chamber, listOf(endStone()), emptyList())?.recipe?.id)
    }

    @Test
    fun specificIngredientIsTakenBeforeOre() {
        val oak = ItemStack(Blocks.LOG)
        val recipes = MachineRecipes(listOf(
            recipe("a", listOf(ItemIngredient.OreDict("logWood", 1), ItemIngredient.Item(oak.copy()))),
        ))
        // slot 0 is the only oak log. The specific ingredient takes it, so the ore ingredient needs slot 1
        val match = recipes.find(chamber, listOf(oak, ItemStack(Blocks.LOG2)), emptyList())
        assertArrayEquals(intArrayOf(1, 1), match?.itemsTaken)
    }

    @Test
    fun acceptsOnlyUsefulInput() {
        val recipes = MachineRecipes(listOf(
            recipe("a", listOf(need(4))),
            recipe("b", fluids = listOf(FluidStack(lava, 1))),
        ))
        assertTrue(recipes.acceptsItem(chamber, endStone()))
        assertTrue(!recipes.acceptsItem(chamber, endStone(1, meta = 1)))
        assertTrue(recipes.acceptsFluid(chamber, lava))
        assertTrue(!recipes.acceptsFluid(chamber, water))
    }

    @Test
    fun validationRejectsBrokenRecipes() {
        val ok = recipe("a", listOf(need(1)))
        assertNull(MachineRecipes.validate(ok))
        assertNotNull(MachineRecipes.validate(ok.copy(itemInputs = emptyList())))
        assertNotNull(MachineRecipes.validate(ok.copy(itemOutputs = emptyList(), fluidOutputs = emptyList())))
        assertNotNull(MachineRecipes.validate(ok.copy(ticks = 0)))
        assertNotNull(MachineRecipes.validate(ok.copy(itemInputs = listOf(need(0)))))
        assertNotNull(MachineRecipes.validate(ok.copy(itemOutputs = listOf(ItemStack.EMPTY))))
        assertNotNull(MachineRecipes.validate(ok.copy(fluidOutputs = listOf(FluidStack(water, 0)))))
    }
}
