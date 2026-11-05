package io.github.srdjanv.endreforked.common.items.base;

import net.minecraft.block.Block;
import net.minecraft.item.ItemSeedFood;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ItemSeedFoodBase extends ItemSeedFood implements InventoryItemModel {

    public ItemSeedFoodBase(String name, int healAmount, float saturation, Block crops, Block soil) {
        super(healAmount, saturation, crops, soil);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReforked.ENDERTAB);
    }
}
