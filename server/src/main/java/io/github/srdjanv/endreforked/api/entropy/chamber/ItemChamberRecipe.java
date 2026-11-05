package io.github.srdjanv.endreforked.api.entropy.chamber;

import java.util.function.Function;

import net.minecraft.item.ItemStack;

public class ItemChamberRecipe extends ChamberRecipe<ItemStack, ItemStack> {

    public ItemChamberRecipe(ItemStack input, int ticksToComplete, int entropyCost,
                             Function<ItemStack, ItemStack> recipeFunction) {
        super(input, ticksToComplete, entropyCost, recipeFunction);
    }
}
