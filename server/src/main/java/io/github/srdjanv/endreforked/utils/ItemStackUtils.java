package io.github.srdjanv.endreforked.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public final class ItemStackUtils {

    public static final String DATA_ID = "mekData";

    @Nonnull
    public static NBTTagCompound getDataMap(ItemStack stack) {
        initStack(stack);

        return stack.getTagCompound().getCompoundTag(DATA_ID);
    }

    @Nullable
    public static NBTTagCompound getDataMapIfPresent(ItemStack stack) {
        return hasDataTag(stack) ? getDataMap(stack) : null;
    }

    @Nonnull
    public static NBTTagCompound getDataMapIfPresentNN(ItemStack stack) {
        return hasDataTag(stack) ? getDataMap(stack) : new NBTTagCompound();
    }

    public static boolean hasData(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return false;
        }

        return getDataMap(stack).hasKey(key);
    }

    public static void removeData(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return;
        }

        getDataMap(stack).removeTag(key);
    }

    public static int getInt(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return 0;
        }

        return getDataMap(stack).getInteger(key);
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return false;
        }

        return getDataMap(stack).getBoolean(key);
    }

    public static double getDouble(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return 0;
        }

        return getDataMap(stack).getDouble(key);
    }

    public static String getString(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return "";
        }

        return getDataMap(stack).getString(key);
    }

    public static NBTTagCompound getCompound(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return new NBTTagCompound();
        }

        return getDataMap(stack).getCompoundTag(key);
    }

    public static NBTTagList getList(ItemStack stack, String key) {
        if (!hasDataTag(stack)) {
            return new NBTTagList();
        }

        return getDataMap(stack).getTagList(key, Constants.NBT.TAG_COMPOUND);
    }

    public static void setInt(ItemStack stack, String key, int i) {
        initStack(stack);

        getDataMap(stack).setInteger(key, i);
    }

    public static void setBoolean(ItemStack stack, String key, boolean b) {
        initStack(stack);

        getDataMap(stack).setBoolean(key, b);
    }

    public static void setDouble(ItemStack stack, String key, double d) {
        initStack(stack);

        getDataMap(stack).setDouble(key, d);
    }

    public static void setString(ItemStack stack, String key, String s) {
        initStack(stack);

        getDataMap(stack).setString(key, s);
    }

    public static void setCompound(ItemStack stack, String key, NBTTagCompound tag) {
        initStack(stack);

        getDataMap(stack).setTag(key, tag);
    }

    public static void setList(ItemStack stack, String key, NBTTagList tag) {
        initStack(stack);

        getDataMap(stack).setTag(key, tag);
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

    public static void dropItemInWorldExact(World world, double x, double y, double z, ItemStack stack) {
        world.spawnEntity(new EntityItem(world, x, y, z, stack));
    }

    public static ItemStack consumeItem(ItemStack stack) {
        return consumeItem(stack, 1);
    }

    public static ItemStack consumeItem(ItemStack stack, int amount) {
        if (stack.getCount() > amount) {
            stack.shrink(amount);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack consumeItem(EntityPlayer player, ItemStack stack) {
        return player.isCreative() ? stack : consumeItem(stack, 1);
    }

    public static ItemStack consumeItem(EntityPlayer player, ItemStack stack, int amount) {
        return player.isCreative() ? stack : consumeItem(stack, amount);
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
