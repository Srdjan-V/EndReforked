package io.github.srdjanv.endreforked.compat.jei.entropychamber

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.registry.MachineRecipeDefaults
import io.github.srdjanv.endreforked.compat.jei.MachineRecipeWrapper
import io.github.srdjanv.endreforked.compat.jei.RecipeFeed
import io.github.srdjanv.endreforked.utils.LangUtil
import mezz.jei.api.IGuiHelper
import mezz.jei.api.gui.IDrawable
import mezz.jei.api.gui.IRecipeLayout
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.IRecipeCategory

class EntropyChamberCategory(helper: IGuiHelper) : IRecipeCategory<MachineRecipeWrapper> {
    private val background = helper.createBlankDrawable(160, 60)

    override fun getUid() = UID

    override fun getTitle(): String = LangUtil.translateToLocal("tile.entropy_chamber.name")

    override fun getModName() = Tags.MODNAME

    override fun getBackground(): IDrawable = background

    override fun setRecipe(layout: IRecipeLayout, recipe: MachineRecipeWrapper, ingredients: IIngredients) {
        // Items in the top row, fluids below them when there are both. Inputs on the left, outputs on the right.
        val fluidY = if (recipe.itemInputs + recipe.itemOutputs > 0) 38 else 20
        val items = layout.itemStacks
        for (i in 0 until recipe.itemInputs) items.init(i, true, 48 - i * 18, 20)
        for (i in 0 until recipe.itemOutputs) items.init(recipe.itemInputs + i, false, 96 + i * 18, 20)
        val fluids = layout.fluidStacks
        for (i in 0 until recipe.fluidInputs) fluids.init(i, true, 48 - i * 18, fluidY)
        for (i in 0 until recipe.fluidOutputs) fluids.init(recipe.fluidInputs + i, false, 96 + i * 18, fluidY)
        items.set(ingredients)
        fluids.set(ingredients)
    }

    companion object {
        const val UID = Tags.MODID + ".entropy_chamber"

        @JvmField
        val FEED = RecipeFeed.machine(UID, MachineRecipeDefaults.ENTROPY_CHAMBER)
    }
}
