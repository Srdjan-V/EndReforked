package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel
import net.minecraft.block.BlockFire

class BlockEntropyFire : BlockFire(), InventoryBlockModel {
    init {
        setTranslationKey("entropy_fire")
        setRegistryName("entropy_fire")
        setCreativeTab(EndReforked.ENDERTAB)
    }
}
