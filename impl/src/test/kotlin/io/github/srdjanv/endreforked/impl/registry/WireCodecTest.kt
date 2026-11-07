package io.github.srdjanv.endreforked.impl.registry

import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.impl.machine.MachineRecipes
import io.github.srdjanv.endreforked.impl.machine.toMachineRecipes
import io.github.srdjanv.endreforked.impl.machine.toSnapshot
import net.minecraft.init.Bootstrap
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class WireCodecTest {
    init {
        Bootstrap.register()
    }

    private fun nbt(key: String, value: String) = NBTTagCompound().apply { setString(key, value) }

    @Test
    fun itemStackKeepsCountMetaAndNbt() {
        val stack = ItemStack(Items.DIAMOND_PICKAXE, 1, 7).apply { tagCompound = nbt("k", "v") }
        val back = stack.toWire().toItemStack()
        assertTrue(ItemStack.areItemStacksEqual(stack, back))
        assertEquals("v", back.tagCompound!!.getString("k"))
    }

    @Test
    fun wildcardMetaIngredientSurvivesARecipeSnapshot() {
        val recipe = MachineRecipe(
            ResourceLocation("test", "r"), ResourceLocation("endreborn", "entropy_chamber"),
            listOf(ItemIngredient.Item(ItemStack(Items.APPLE, 3, 32767))),
            itemOutputs = listOf(ItemStack(Items.DIAMOND, 2).apply { tagCompound = nbt("a", "b") }), ticks = 10,
        )
        val back = MachineRecipes(listOf(recipe)).toSnapshot().toMachineRecipes()!!.single()
        val input = (back.itemInputs.single() as ItemIngredient.Item).stack
        assertEquals(32767, input.metadata)
        assertEquals(3, input.count)
        assertEquals("b", back.itemOutputs.single().tagCompound!!.getString("a"))
    }

    @Test
    fun fluidStackKeepsAmountAndNbt() {
        val stack = FluidStack(FluidRegistry.LAVA, 750, nbt("f", "x"))
        val back = stack.toWire().toFluidStack()
        assertTrue(stack.isFluidStackIdentical(back))
        assertEquals("x", back.tag!!.getString("f"))
    }
}
