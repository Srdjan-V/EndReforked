package io.github.srdjanv.endreforked.common.items.tools;

import net.minecraft.item.ItemHoe;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ToolHoe extends ItemHoe implements InventoryItemModel {

    public ToolHoe(String name, ToolMaterial material) {
        super(material);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReforked.ENDERTAB);
    }
}
