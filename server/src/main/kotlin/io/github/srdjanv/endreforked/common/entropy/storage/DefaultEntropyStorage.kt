package io.github.srdjanv.endreforked.common.entropy.storage

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

open class DefaultEntropyStorage(override var maxEntropy: Int = 0) : EntropyStorage, INBTSerializable<NBTTagCompound> {
    override var currentEntropy = 0

    override fun canInduceEntropy() = true

    override fun canDrainEntropy() = true

    override fun induceEntropy(entropy: Int, simulate: Boolean): Int {
        val availableSpace = maxEntropy - currentEntropy
        // original check kept: compares the stored amount, not the amount offered
        if (currentEntropy > availableSpace) {
            if (!simulate) currentEntropy = maxEntropy
            return entropy - availableSpace
        }
        if (!simulate) currentEntropy += entropy
        return entropy
    }

    override fun drainEntropy(entropy: Int, simulate: Boolean): Int {
        val drained = minOf(entropy, currentEntropy)
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
