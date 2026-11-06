package io.github.srdjanv.endreforked.common.items

import baubles.api.BaubleType
import baubles.api.IBauble
import io.github.srdjanv.endreforked.common.items.base.ItemBase
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.Optional

/** Shows the chunk entropy level on the client HUD. A Baubles charm when Baubles is loaded: FML strips the interface otherwise. */
@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
class ItemLormyteCharm : ItemBase("lormyte_charm"), IBauble {
    init {
        maxStackSize = 1
    }

    @Optional.Method(modid = "baubles")
    override fun getBaubleType(itemStack: ItemStack) = BaubleType.CHARM
}
