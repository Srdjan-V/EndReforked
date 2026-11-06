package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.api.entropy.EntropyWings
import io.github.srdjanv.endreforked.common.items.base.ItemBase

open class ItemEntropyWings(name: String = "entropy_wings") : ItemBase(name), EntropyWings {
    override val entropyCost get() = 250
    override val entropyRadius get() = EntropyRadius.TWO
    override val flightDuration get() = 25 * 20
}
