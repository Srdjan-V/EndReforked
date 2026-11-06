package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.sounds.EndSound
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.item.ItemRecord
import net.minecraft.util.text.translation.I18n
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemEndRecord(name: String, soundIn: EndSound) : ItemRecord(soundIn.name, soundIn), InventoryItemModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }

    @SideOnly(Side.CLIENT)
    @Suppress("DEPRECATION")
    override fun getRecordNameLocal(): String = I18n.translateToLocal("item.endreborn.record.end.desc")
}
