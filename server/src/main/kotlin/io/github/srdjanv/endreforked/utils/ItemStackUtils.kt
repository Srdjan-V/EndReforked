package io.github.srdjanv.endreforked.utils

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

object ItemStackUtils {
    private const val DATA_ID = "mekData"

    @JvmStatic
    fun getInt(stack: ItemStack, key: String): Int =
        if (stack.tagCompound?.hasKey(DATA_ID) == true) dataMap(stack).getInteger(key) else 0

    @JvmStatic
    fun setInt(stack: ItemStack, key: String, value: Int) = dataMap(stack).setInteger(key, value)

    @JvmStatic
    fun copyStackWithAmount(stack: ItemStack, amount: Int): ItemStack =
        if (stack.isEmpty) ItemStack.EMPTY else stack.copy().also { it.count = amount }

    private fun dataMap(stack: ItemStack): NBTTagCompound {
        val tag = stack.tagCompound ?: NBTTagCompound().also { stack.tagCompound = it }
        if (!tag.hasKey(DATA_ID)) tag.setTag(DATA_ID, NBTTagCompound())
        return tag.getCompoundTag(DATA_ID)
    }
}
