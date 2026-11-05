package io.github.srdjanv.endreforked.common.items;

import net.minecraft.item.ItemRecord;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.common.sounds.EndSound;
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel;

public class ItemEndRecord extends ItemRecord implements InventoryItemModel {

    public ItemEndRecord(String name, EndSound soundIn) {
        super(soundIn.name, soundIn);
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReforked.ENDERTAB);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getRecordNameLocal() {
        return I18n.translateToLocal("item.endreborn.record.end.desc");
    }
}
