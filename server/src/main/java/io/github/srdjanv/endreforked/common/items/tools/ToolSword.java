package io.github.srdjanv.endreforked.common.items.tools;

import net.minecraft.item.ItemSword;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ToolSword extends ItemSword implements InventoryItemModel {

    public ToolSword(String name, ToolMaterial material) {
        super(material);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReforked.ENDERTAB);
    }
}
