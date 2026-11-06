package io.github.srdjanv.endreforked.common.items.tools

import io.github.srdjanv.endreforked.EndReforked
import net.minecraft.item.ItemBow

open class ToolBow(name: String) : ItemBow() {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
        setMaxStackSize(1)
    }
}
