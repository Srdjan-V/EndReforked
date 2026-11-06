package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.items.base.ItemBase
import net.minecraft.item.ItemStack
import java.util.Random

class ItemHammer(name: String) : ItemBase(name) {
    init {
        setMaxDamage(64)
        setMaxStackSize(1)
    }

    override fun hasContainerItem(stack: ItemStack) = true

    override fun getContainerItem(itemStack: ItemStack): ItemStack =
        itemStack.copy().also { it.attemptDamageItem(2, Random(), null) }

    override fun isRepairable() = false
}
