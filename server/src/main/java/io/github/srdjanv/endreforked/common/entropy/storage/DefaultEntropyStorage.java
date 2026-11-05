package io.github.srdjanv.endreforked.common.entropy.storage;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage;

public class DefaultEntropyStorage implements EntropyStorage, INBTSerializable<NBTTagCompound> {

    private int capacity;
    private int entropy;

    public DefaultEntropyStorage() {}

    public DefaultEntropyStorage(int capacity) {
        this(capacity, 0);
    }

    public DefaultEntropyStorage(int capacity, int entropy) {
        this.capacity = capacity;
        this.entropy = entropy;
    }

    @Override
    public int getMaxEntropy() {
        return capacity;
    }

    @Override
    public int getCurrentEntropy() {
        return entropy;
    }

    @Override
    public int induceEntropy(int entropy, boolean simulate) {
        if (!canInduceEntropy()) return 0;
        int availableSpace = getMaxEntropy() - getCurrentEntropy();
        if (this.entropy > availableSpace) {
            if (!simulate) this.entropy = getMaxEntropy();
            return entropy - availableSpace;
        }
        if (!simulate) this.entropy += entropy;
        return entropy;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setEntropy(int entropy) {
        this.entropy = entropy;
    }

    @Override
    public boolean canInduceEntropy() {
        return true;
    }

    @Override
    public int drainEntropy(int entropy, boolean simulate) {
        if (!canDrainEntropy()) return 0;

        if (entropy > this.entropy) {
            entropy = this.entropy;
            if (!simulate) this.entropy = 0;
            return entropy;
        }
        if (!simulate) this.entropy -= entropy;
        return entropy;
    }

    @Override
    public boolean canDrainEntropy() {
        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        var tag = new NBTTagCompound();
        tag.setInteger("capacity", capacity);
        tag.setInteger("entropy", entropy);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        capacity = nbt.getInteger("capacity");
        entropy = nbt.getInteger("entropy");
    }
}
