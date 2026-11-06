package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.api.entropy.EntropyRadiusUpgrade
import io.github.srdjanv.endreforked.common.items.base.ItemBase

class ItemEntropyRadiusUpgrade(override val entropyRadius: EntropyRadius) :
    ItemBase("entropy_radius_upgrade_" + entropyRadius.ordinal), EntropyRadiusUpgrade {
    init {
        setMaxStackSize(1)
    }
}
