package io.github.srdjanv.endreforked.compat.jei.materializer;

import java.util.List;

import net.minecraft.item.ItemStack;

import com.google.common.collect.Lists;

import io.github.srdjanv.endreforked.utils.TooltipUtils;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;

public class MaterializerRecipe implements IRecipeWrapper {

    private final ItemStack input1, input2;
    private final String input1Tooltip, input2Tooltip;
    private final ItemStack output;

    public MaterializerRecipe(ItemStack input1, String input1Tooltip,
                              ItemStack input2, String input2Tooltip,
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
        ingredients.setInputs(VanillaTypes.ITEM, Lists.newArrayList(input2, input1));
        ingredients.setOutput(VanillaTypes.ITEM, output);
    }
}
