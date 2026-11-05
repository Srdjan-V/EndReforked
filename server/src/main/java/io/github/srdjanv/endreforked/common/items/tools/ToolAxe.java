package io.github.srdjanv.endreforked.common.items.tools;

import net.minecraft.item.ItemAxe;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ToolAxe extends ItemAxe implements InventoryItemModel {

    public ToolAxe(String name, ToolMaterial material) {
        super(material, 0, 0);
        if (ModItems.TOOL_ENDORIUM.get().getClass().isInstance(material)) {
            this.attackDamage = 9;
            this.attackSpeed = -3.1f;
        } else if (ModItems.TUNGSTEN.get().getClass().isInstance(material)) {
            this.attackDamage = 8;
            this.attackSpeed = -3;
        }

        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReforked.ENDERTAB);
    }
}
