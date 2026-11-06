package io.github.srdjanv.endreforked.common.entropy.storage

import io.github.srdjanv.endreforked.api.capabilities.entropy.WeakEntropyStorage
import net.minecraft.nbt.NBTTagCompound

class DefaultWeakEntropyStorage(capacity: Int, override var decay: Int) : DefaultEntropyStorage(capacity), WeakEntropyStorage {
    override var loadFactor = 0.8

    override val isOverLoaded get() = currentEntropy.toDouble() / maxEntropy > loadFactor

    override fun induceEntropy(entropy: Int, simulate: Boolean) =
        super.induceEntropy(entropy, simulate).also { if (!simulate && isOverLoaded) super.drainEntropy(decay, false) }

    override fun drainEntropy(entropy: Int, simulate: Boolean) =
        super.drainEntropy(entropy, simulate).also { if (!simulate && isOverLoaded) super.drainEntropy(decay, false) }

    override fun serializeNBT() = super.serializeNBT().apply {
        setInteger("decay", decay)
        setDouble("load_factor", loadFactor)
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        super.deserializeNBT(nbt)
        decay = nbt.getInteger("decay")
        loadFactor = nbt.getDouble("load_factor")
    }
}
