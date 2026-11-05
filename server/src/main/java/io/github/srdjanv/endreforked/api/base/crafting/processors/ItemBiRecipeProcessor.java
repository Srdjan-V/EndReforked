package io.github.srdjanv.endreforked.api.base.crafting.processors;

import java.util.Objects;

import net.minecraft.item.ItemStack;

import io.github.srdjanv.endreforked.api.base.crafting.HandlerGroupingRegistry;
import io.github.srdjanv.endreforked.api.base.crafting.groupings.RecipeGrouping;
import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.BiRecipe;

public class ItemBiRecipeProcessor<OUT,
        RG extends RecipeGrouping<ItemStack, ItemStack, R>,
        R extends BiRecipe<ItemStack, ItemStack, OUT>>
                                  extends BaseBiRecipeProcessor<ItemStack, ItemStack, OUT, RG, R> {

    public ItemBiRecipeProcessor(HandlerGroupingRegistry<ItemStack, ItemStack, OUT, RG, R> handlerGroupingRegistry) {
        super(handlerGroupingRegistry);
    }

    @Override
    public boolean validateGrouping(ItemStack input) {
        if (Objects.isNull(input)) return false;
        if (input.isEmpty()) return false;
        return super.validateGrouping(input);
    }

    @Override
    public boolean validateRecipe(ItemStack input) {
        if (Objects.isNull(input)) return false;
        if (input.isEmpty()) return false;
        return super.validateRecipe(input);
    }
}
