package io.github.srdjanv.endreforked.compat.jei

import com.cleanroommc.modularui.utils.Color
import io.github.srdjanv.endreforked.api.machine.ItemIngredient
import io.github.srdjanv.endreforked.api.machine.MachineRecipe
import io.github.srdjanv.endreforked.utils.LangUtil
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.ingredients.VanillaTypes
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.oredict.OreDictionary

/** A machine recipe as JEI shows it, for any machine. Built from the client registry. It shows the entropy cost when there is one. */
class MachineRecipeWrapper private constructor(
    val recipe: MachineRecipe,
    private val itemIn: List<List<ItemStack>>,
    private val itemOut: List<ItemStack>,
    private val fluidIn: List<FluidStack>,
    private val fluidOut: List<FluidStack>,
) : IRecipeWrapper {
    val itemInputs get() = itemIn.size
    val itemOutputs get() = itemOut.size
    val fluidInputs get() = fluidIn.size
    val fluidOutputs get() = fluidOut.size

    override fun drawInfo(minecraft: Minecraft, recipeWidth: Int, recipeHeight: Int, mouseX: Int, mouseY: Int) {
        if (recipe.entropy > 0) minecraft.fontRenderer.drawString(recipe.entropy.toString(), 76, 47, Color.argb(128, 128, 128, 1))
    }

    override fun getTooltipStrings(mouseX: Int, mouseY: Int): List<String> {
        if (recipe.entropy > 0 && mouseX in 73..84 && mouseY in 44..55) {
            return listOf(LangUtil.translateToLocal("endreborn.jei.entropy_wand.item_cost"))
        }
        return super.getTooltipStrings(mouseX, mouseY)
    }

    override fun getIngredients(ingredients: IIngredients) {
        if (itemIn.isNotEmpty()) ingredients.setInputLists(VanillaTypes.ITEM, itemIn)
        if (itemOut.isNotEmpty()) ingredients.setOutputs(VanillaTypes.ITEM, itemOut)
        if (fluidIn.isNotEmpty()) ingredients.setInputs(VanillaTypes.FLUID, fluidIn)
        if (fluidOut.isNotEmpty()) ingredients.setOutputs(VanillaTypes.FLUID, fluidOut)
    }

    companion object {
        /** Returns null when an ore dictionary ingredient has no item on this client. */
        fun of(recipe: MachineRecipe): MachineRecipeWrapper? {
            val itemIn = recipe.itemInputs.map { ing ->
                when (ing) {
                    is ItemIngredient.Item -> listOf(ing.stack)
                    is ItemIngredient.OreDict -> OreDictionary.getOres(ing.name).map { it.copy().also { s -> s.count = ing.count } }
                }
            }
            if (itemIn.any { it.isEmpty() }) return null
            return MachineRecipeWrapper(recipe, itemIn, recipe.itemOutputs, recipe.fluidInputs, recipe.fluidOutputs)
        }
    }
}
