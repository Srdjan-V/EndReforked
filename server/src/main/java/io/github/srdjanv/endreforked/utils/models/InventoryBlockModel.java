package io.github.srdjanv.endreforked.utils.models;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import io.github.srdjanv.endreforked.EndReforked;

public interface InventoryBlockModel extends IAsset {

    @Override
    default void handleAssets() {
        EndReforked.getProxy().registerItemRenderer(Item.getItemFromBlock((Block) this), 0, "inventory");
    }
}
