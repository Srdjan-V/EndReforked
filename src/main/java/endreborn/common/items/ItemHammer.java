package endreborn.common.items;

import java.util.Random;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import endreborn.EndReborn;
import endreborn.common.ModItems;
import endreborn.utils.IHasModel;

public class ItemHammer extends Item implements IHasModel {

    public ItemHammer(String name) {
        setTranslationKey(name);
        setRegistryName(name);
        setMaxDamage(64);
        setCreativeTab(EndReborn.endertab);
        setMaxStackSize(1);

        ModItems.ITEMS.add(this);
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

    @Override
    public void registerModels() {
        EndReborn.proxy.registerItemRenderer(this, 0, "inventory");
    }
}