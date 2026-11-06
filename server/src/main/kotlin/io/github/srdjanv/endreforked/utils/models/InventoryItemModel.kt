package io.github.srdjanv.endreforked.utils.models

import io.github.srdjanv.endreforked.EndReforked
import net.minecraft.item.Item

interface InventoryItemModel : IAsset {
    override fun handleAssets() = EndReforked.getProxy().registerItemRenderer(this as Item, 0, "inventory")
}
