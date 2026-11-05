package io.github.srdjanv.endreforked.compat.jei.endforge

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.registry.MachineRecipeDefaults
import io.github.srdjanv.endreforked.compat.jei.MachineCategory
import io.github.srdjanv.endreforked.compat.jei.MachineRecipeWrapper
import io.github.srdjanv.endreforked.compat.jei.RecipeFeed
import mezz.jei.api.IGuiHelper
import mezz.jei.api.gui.IRecipeLayout
import mezz.jei.api.ingredients.IIngredients

/** The fluid at x 35, the item at x 55 and the output at x 105. */
class EndForgeCategory(helper: IGuiHelper) : MachineCategory(helper, UID, "tile.block_end_forge.name") {
    override fun setRecipe(layout: IRecipeLayout, recipe: MachineRecipeWrapper, ingredients: IIngredients) {
        layout.fluidStacks.init(0, true, 35, 20, 18, 18, 16000, true, null)
        layout.fluidStacks.set(ingredients)
        layout.itemStacks.init(0, true, 55, 20)
        layout.itemStacks.init(1, false, 105, 20)
        layout.itemStacks.set(ingredients)
    }

    companion object {
        const val UID = Tags.MODID + ".endforge"

        @JvmField
        val FEED = RecipeFeed(UID, MachineRecipeDefaults.END_FORGE)
    }
}
