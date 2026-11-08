package io.github.srdjanv.endreforked.utils.models

import io.github.srdjanv.endreforked.EndReforked
import net.minecraft.block.Block
import net.minecraft.item.Item

interface InventoryBlockModel : IAsset {
    override fun handleAssets() = EndReforked.getProxy().registerItemRenderer(Item.getItemFromBlock(this as Block), 0, "inventory")
}
