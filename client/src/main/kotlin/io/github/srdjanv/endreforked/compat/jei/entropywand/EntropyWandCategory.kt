package io.github.srdjanv.endreforked.compat.jei.entropywand

import com.cleanroommc.modularui.drawable.ItemDrawable
import com.cleanroommc.modularui.utils.Color
import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.world.WandConversion
import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.configs.Configs
import io.github.srdjanv.endreforked.common.registry.GameAdapter
import io.github.srdjanv.endreforked.common.registry.GameAdapter.matches
import io.github.srdjanv.endreforked.compat.jei.RecipeFeed
import io.github.srdjanv.endreforked.impl.world.WandConversions
import io.github.srdjanv.endreforked.utils.LangUtil
import mezz.jei.api.IGuiHelper
import mezz.jei.api.gui.IDrawable
import mezz.jei.api.gui.IRecipeLayout
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.ingredients.VanillaTypes
import mezz.jei.api.recipe.IRecipeCategory
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack

/** A wand conversion as JEI shows it: the blocks that fit as input, the result as output, and the wand damage. */
class WandConversionWrapper private constructor(
    private val input: List<ItemStack>,
    private val output: ItemStack,
    private val damage: Int,
) : IRecipeWrapper {
    override fun drawInfo(minecraft: Minecraft, recipeWidth: Int, recipeHeight: Int, mouseX: Int, mouseY: Int) {
        minecraft.fontRenderer.drawString(damage.toString(), 76, 47, Color.argb(128, 128, 128, 1))
    }

    override fun getTooltipStrings(mouseX: Int, mouseY: Int): List<String> {
        if (mouseX in 73..84 && mouseY in 44..55) return listOf(LangUtil.translateToLocal("endreborn.jei.entropy_wand.item_cost"))
        return super.getTooltipStrings(mouseX, mouseY)
    }

    override fun getIngredients(ingredients: IIngredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, listOf(input))
        ingredients.setOutput(VanillaTypes.ITEM, output)
    }

    companion object {
        /** Returns null when this client does not have the blocks, or no input has an item. */
        fun of(conversion: WandConversion): WandConversionWrapper? {
            val block = GameAdapter.block(conversion.match.block) ?: return null
            val result = GameAdapter.state(conversion.result) ?: return null
            var input = block.blockState.validStates.filter { it.matches(conversion.match) }
                .map { ItemStack(block, 1, block.getMetaFromState(it)) }
                .filter { !it.isEmpty }
                .distinctBy { it.metadata }
            if (!Configs.CLIENT_SIDE_CONFIGS.entropyWandRenderBrokenTextures) {
                val mesher = Minecraft.getMinecraft().renderItem.itemModelMesher
                input = input.filter { mesher.getItemModel(it) !== mesher.modelManager.missingModel }
            }
            val output = ItemStack(result.block, 1, result.block.getMetaFromState(result))
            if (input.isEmpty() || output.isEmpty) return null
            return WandConversionWrapper(input, output, conversion.itemDamage ?: Configs.SERVER_SIDE_CONFIGS.entropyWandUseDamage)
        }
    }
}

class EntropyWandCategory(helper: IGuiHelper) : IRecipeCategory<WandConversionWrapper> {
    private val background = helper.createBlankDrawable(160, 60)
    private val slot = helper.slotDrawable

    override fun getUid() = UID

    override fun getTitle(): String = LangUtil.translateToLocal("item.entropy_wand.name")

    override fun getModName() = Tags.MODNAME

    override fun getBackground(): IDrawable = background

    override fun drawExtras(minecraft: Minecraft) {
        slot.draw(minecraft, 48, 20)
        wandItem.draw(null, 72, 20, 16, 16)
        slot.draw(minecraft, 96, 20)
    }

    override fun setRecipe(layout: IRecipeLayout, recipe: WandConversionWrapper, ingredients: IIngredients) {
        val stacks = layout.itemStacks
        stacks.init(0, true, 48, 20)
        stacks.init(1, false, 96, 20)
        stacks.set(ingredients)
    }

    companion object {
        const val UID = Tags.MODID + ".entropywand"

        private val wandItem by lazy { ItemDrawable(ItemStack(ModItems.ENTROPY_WAND.get())) }

        @JvmField
        val FEED = RecipeFeed<WandConversion, WandConversions>(UID) { snapshot -> snapshot.entries.mapNotNull(WandConversionWrapper::of) }
    }
}
