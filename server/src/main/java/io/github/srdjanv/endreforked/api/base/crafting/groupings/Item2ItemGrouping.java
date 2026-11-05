package io.github.srdjanv.endreforked.api.base.crafting.groupings;

import net.minecraft.item.ItemStack;

import io.github.srdjanv.endreforked.api.base.crafting.recipe.base.BiRecipe;
import io.github.srdjanv.endreforked.api.util.ItemStackHashStrategy;

public class Item2ItemGrouping<R extends BiRecipe<ItemStack, ItemStack, ?>>
                              extends RecipeGrouping<ItemStack, ItemStack, R> {

    public Item2ItemGrouping(ItemStack catalyst, ItemStackHashStrategy strategy) {
        super(catalyst, strategy);
    }

    public Item2ItemGrouping(ItemStack catalyst) {
        super(catalyst, ItemStackHashStrategy.memorizedComparingAllButCount());
    }
}
