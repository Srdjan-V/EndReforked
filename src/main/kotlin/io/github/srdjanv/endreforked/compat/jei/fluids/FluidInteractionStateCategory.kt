package io.github.srdjanv.endreforked.compat.jei.fluids

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.api.world.FluidBlockInteraction
import io.github.srdjanv.endreforked.compat.jei.RecipeFeed
import io.github.srdjanv.endreforked.impl.world.FluidBlockInteractions
import io.github.srdjanv.endreforked.utils.LangUtil
import mezz.jei.api.IGuiHelper
import mezz.jei.api.gui.IDrawable
import mezz.jei.api.gui.IRecipeLayout
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.ingredients.VanillaTypes
import mezz.jei.api.recipe.IRecipeCategory
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidRegistry
import net.minecraftforge.fluids.FluidStack

/** A block as JEI shows it: a fluid block shows as its fluid, every other block as its item. */
class BlockIngredient(state: IBlockState) {
    val fluid: FluidStack? = FluidRegistry.lookupFluidForBlock(state.block)?.let { FluidStack(it, 1_000) }
    val item: ItemStack = if (fluid == null) ItemStack(state.block, 1, state.block.getMetaFromState(state)) else ItemStack.EMPTY
    val valid get() = fluid != null || !item.isEmpty
}

/** A fluid block interaction as JEI shows it: the fluid and the neighbour block go in, the new block comes out. */
class FluidBlockInteractionWrapper private constructor(
    private val fluid: FluidStack,
    val input: BlockIngredient,
    val output: BlockIngredient,
) : IRecipeWrapper {
    override fun getIngredients(ingredients: IIngredients) {
        ingredients.setInputs(VanillaTypes.FLUID, listOfNotNull(fluid, input.fluid))
        if (!input.item.isEmpty) ingredients.setInput(VanillaTypes.ITEM, input.item)
        output.fluid?.let { ingredients.setOutput(VanillaTypes.FLUID, it) }
        if (!output.item.isEmpty) ingredients.setOutput(VanillaTypes.ITEM, output.item)
    }

    companion object {
        /** Returns null when a block has neither a fluid nor an item. */
        fun of(interaction: FluidBlockInteraction): FluidBlockInteractionWrapper? {
            val input = BlockIngredient(interaction.match.toState())
            val output = BlockIngredient(interaction.result)
            if (!input.valid || !output.valid) return null
            return FluidBlockInteractionWrapper(FluidStack(interaction.fluid, 1_000), input, output)
        }
    }
}

class FluidInteractionStateCategory(helper: IGuiHelper) : IRecipeCategory<FluidBlockInteractionWrapper> {
    private val background = helper.createBlankDrawable(160, 60)

    override fun getUid() = UID

    override fun getTitle(): String = LangUtil.translateToLocal("fluid.interaction.state")

    override fun getModName() = Tags.MODNAME

    override fun getBackground(): IDrawable = background

    override fun setRecipe(layout: IRecipeLayout, recipe: FluidBlockInteractionWrapper, ingredients: IIngredients) {
        val fluids = layout.fluidStacks
        val items = layout.itemStacks
        fluids.init(0, true, 50, 20)
        if (recipe.input.fluid != null) fluids.init(1, true, 25, 20) else items.init(0, true, 25, 20)
        if (recipe.output.fluid != null) fluids.init(2, false, 100, 20) else items.init(1, false, 100, 20)
        fluids.set(ingredients)
        items.set(ingredients)
    }

    companion object {
        const val UID = Tags.MODID + ".fluid_state_interaction"

        @JvmField
        val FEED = RecipeFeed<FluidBlockInteraction, FluidBlockInteractions>(UID) { snapshot ->
            snapshot.entries.mapNotNull(FluidBlockInteractionWrapper::of)
        }
    }
}
