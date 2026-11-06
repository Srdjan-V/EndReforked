package io.github.srdjanv.endreforked.utils;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class ItemStackUtils {

    public static final String DATA_ID = "mekData";

    @Nonnull
    public static NBTTagCompound getDataMap(ItemStack stack) {
        initStack(stack);

        return stack.getTagCompound().getCompoundTag(DATA_ID);
    }

    public static int getInt(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return 0;
        }

        return getDataMap(stack).getInteger(key);
    }

    public static void setInt(ItemStack stack, String key, int i) {
        initStack(stack);

        getDataMap(stack).setInteger(key, i);
    }

    private static boolean hasDataTag(ItemStack stack) {
        return stack.getTagCompound() != null && stack.getTagCompound().hasKey(DATA_ID);
    }

    private static void initStack(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        if (!stack.getTagCompound().hasKey(DATA_ID)) {
            stack.getTagCompound().setTag(DATA_ID, new NBTTagCompound());
        }
    }

    public static ItemStack copyStackWithAmount(ItemStack stack, int amount) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        ItemStack s2 = stack.copy();
        s2.setCount(amount);
        return s2;
    }

    private ItemStackUtils() {}
}
