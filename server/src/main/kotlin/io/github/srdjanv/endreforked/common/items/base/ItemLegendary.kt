package io.github.srdjanv.endreforked.common.items.base

import net.minecraft.item.EnumRarity
import net.minecraft.item.ItemStack

open class ItemLegendary(name: String) : ItemBase(name) {
    override fun getRarity(stack: ItemStack) = EnumRarity.RARE
}
