package io.github.srdjanv.endreforked.common.items;

import java.util.Random;

import net.minecraft.item.ItemStack;

import io.github.srdjanv.endreforked.common.items.base.ItemBase;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ItemHammer extends ItemBase implements InventoryItemModel {

    public ItemHammer(String name) {
        super(name);
        setMaxDamage(64);
        setMaxStackSize(1);
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        ItemStack container = itemStack.copy();
        container.attemptDamageItem(2, new Random(), null);
        return container;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }
}
