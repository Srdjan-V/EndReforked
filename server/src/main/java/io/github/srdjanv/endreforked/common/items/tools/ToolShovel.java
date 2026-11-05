package io.github.srdjanv.endreforked.common.items.tools;

import net.minecraft.item.ItemSpade;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ToolShovel extends ItemSpade implements InventoryItemModel {

    public ToolShovel(String name, ToolMaterial material) {
        super(material);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReforked.ENDERTAB);
    }
}
