package io.github.srdjanv.endreforked.common.items.base

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.block.Block
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList

open class BaseMetaItemBlock(block: Block, protected val nameFunction: Mapper) : ItemBlock(block), InventoryItemModel {
    fun interface Mapper {
        fun apply(meta: Int): String?
    }

    init {
        setHasSubtypes(true)
        setMaxDamage(0)
    }

    override fun getMetadata(damage: Int) = damage

    override fun getTranslationKey(stack: ItemStack): String =
        super.getTranslationKey() + (nameFunction.apply(stack.itemDamage)?.let { "." + it.replace('$', '.') } ?: "")

    override fun handleAssets() {
        val items = NonNullList.create<ItemStack>()
        getSubItems(EndReforked.ENDERTAB, items)
        for (item in items) {
            val meta = item.metadata
            val postfix = nameFunction.apply(meta)?.let { "_" + it.replace('$', '_') } ?: ""
            EndReforked.getProxy().registerItemRenderer(this, meta, postfix, "inventory")
        }
    }
}
