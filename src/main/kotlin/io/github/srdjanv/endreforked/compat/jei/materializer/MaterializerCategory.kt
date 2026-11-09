package io.github.srdjanv.endreforked.compat.jei.materializer

import com.cleanroommc.modularui.drawable.GuiTextures
import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.registry.MachineRecipeDefaults
import io.github.srdjanv.endreforked.compat.jei.MachineCategory
import io.github.srdjanv.endreforked.compat.jei.MachineRecipeWrapper
import io.github.srdjanv.endreforked.compat.jei.RecipeFeed
import mezz.jei.api.IGuiHelper
import mezz.jei.api.gui.IRecipeLayout
import mezz.jei.api.ingredients.IIngredients
import net.minecraft.client.Minecraft

/** The three items at x 15, 35 and 55 (the catalyst is one) and the output at x 105. As before, it shows a help icon and no world events. */
class MaterializerCategory(helper: IGuiHelper) : MachineCategory(helper, UID, "tile.materializer_block.name", listOf(15, 35, 55)) {
    override fun drawExtras(minecraft: Minecraft) {
        super.drawExtras(minecraft)
        GuiTextures.HELP.draw(142f, 42f, 16f, 16f)
    }

    override fun setRecipe(layout: IRecipeLayout, recipe: MachineRecipeWrapper, ingredients: IIngredients) {
        layout.itemStacks.init(0, true, 15, 20)
        layout.itemStacks.init(1, true, 35, 20)
        layout.itemStacks.init(2, true, 55, 20)
        layout.itemStacks.init(3, false, 105, 20)
        layout.itemStacks.set(ingredients)
    }

    companion object {
        const val UID = Tags.MODID + ".materializer"

        @JvmField
        val FEED = RecipeFeed.machine(UID, MachineRecipeDefaults.MATERIALIZER)
    }
}
