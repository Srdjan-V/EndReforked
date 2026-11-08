package io.github.srdjanv.endreforked.common.entropy.storage

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

open class DefaultEntropyStorage(override var maxEntropy: Int = 0) : EntropyStorage, INBTSerializable<NBTTagCompound> {
    override var currentEntropy = 0

    override fun canInduceEntropy() = true

    override fun canDrainEntropy() = true

    override fun induceEntropy(entropy: Int, simulate: Boolean): Int {
        val induced = entropy.coerceIn(0, maxOf(0, maxEntropy - currentEntropy))
        if (!simulate) currentEntropy += induced
        return induced
    }

    override fun drainEntropy(entropy: Int, simulate: Boolean): Int {
        val drained = entropy.coerceIn(0, currentEntropy)
        if (!simulate) currentEntropy -= drained
        return drained
    }

    override fun serializeNBT() = NBTTagCompound().apply {
        setInteger("capacity", maxEntropy)
        setInteger("entropy", currentEntropy)
    }

    override fun deserializeNBT(nbt: NBTTagCompound) {
        maxEntropy = nbt.getInteger("capacity")
        currentEntropy = nbt.getInteger("entropy")
    }
}
