package io.github.srdjanv.endreforked.api.materializer;

import net.minecraft.item.ItemStack;

import io.github.srdjanv.endreforked.api.base.crafting.groupings.Item2ItemGrouping;

public class ItemCatalyst extends Item2ItemGrouping<MaterializerRecipe> {

    public ItemCatalyst(ItemStack catalyst) {
        super(catalyst);
    }
}
