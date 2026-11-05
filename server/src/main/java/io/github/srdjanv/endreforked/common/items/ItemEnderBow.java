package io.github.srdjanv.endreforked.common.items;

import net.minecraft.item.ItemStack;

import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.common.items.tools.ToolBow;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ItemEnderBow extends ToolBow implements InventoryItemModel {

    public ItemEnderBow(String name) {
        super(name);
        setMaxDamage(1024);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == ModItems.INGOT_ENDORIUM.get() || super.getIsRepairable(toRepair, repair);
    }
}
