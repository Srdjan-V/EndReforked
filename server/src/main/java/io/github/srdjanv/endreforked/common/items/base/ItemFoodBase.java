package io.github.srdjanv.endreforked.common.items.base;

import net.minecraft.item.ItemFood;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ItemFoodBase extends ItemFood implements InventoryItemModel {

    public ItemFoodBase(String name, int amount, float saturation, boolean isWolfFood) {
        super(amount, saturation, isWolfFood);
        config(name);
    }

    public ItemFoodBase(String name, int amount, boolean isWolfFood) {
        super(amount, isWolfFood);
        config(name);
    }

    private void config(String name) {
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReforked.ENDERTAB);
    }
}
