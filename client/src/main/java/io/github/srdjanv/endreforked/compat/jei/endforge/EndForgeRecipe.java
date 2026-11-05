package io.github.srdjanv.endreforked.compat.jei.endforge;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import io.github.srdjanv.endreforked.utils.TooltipUtils;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class EndForgeRecipe implements IRecipeWrapper {

    private final FluidStack input1;
    private final ItemStack input2, output;
    private final String input1Tooltip, input2Tooltip;

    public EndForgeRecipe(FluidStack input1, String input1Tooltip, ItemStack input2, String input2Tooltip,
                          ItemStack output) {
        this.input1 = input1;
        this.input1Tooltip = input1Tooltip;
        this.input2 = input2;
        this.input2Tooltip = input2Tooltip;
        this.output = output;
    }

    public void applyInput1Tooltip(List<String> tooltip) {
        TooltipUtils.addBeforeLast(tooltip, input1Tooltip);
    }

    public void applyInput2Tooltip(List<String> tooltip) {
        TooltipUtils.addBeforeLast(tooltip, input2Tooltip);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInput(VanillaTypes.FLUID, input1);
        ingredients.setInput(VanillaTypes.ITEM, input2);
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }
}
