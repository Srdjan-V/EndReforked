package io.github.srdjanv.endreforked.compat.baubles

import baubles.api.BaubleType
import baubles.api.IBauble
import io.github.srdjanv.endreforked.common.items.ItemEntropyWings
import net.minecraft.item.ItemStack

class EntropyWingsRing : ItemEntropyWings("entropy_wings_ring"), IBauble {
    override fun getBaubleType(itemStack: ItemStack) = BaubleType.RING
}
