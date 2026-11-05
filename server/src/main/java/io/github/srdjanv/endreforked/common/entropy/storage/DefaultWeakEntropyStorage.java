package io.github.srdjanv.endreforked.common.entropy.storage;

import net.minecraft.nbt.NBTTagCompound;

import io.github.srdjanv.endreforked.api.capabilities.entropy.WeakEntropyStorage;

public class DefaultWeakEntropyStorage extends DefaultEntropyStorage implements WeakEntropyStorage {

    private double loadFactor;
    private int decay;

    public DefaultWeakEntropyStorage(int capacity, int entropy, int decay) {
        super(capacity, entropy);
        this.decay = decay;
        this.loadFactor = 0.8;
    }

    public DefaultWeakEntropyStorage(int capacity, int decay) {
        super(capacity);
        this.decay = decay;
        this.loadFactor = 0.8;
    }

    @Override
    public int induceEntropy(int entropy, boolean simulate) {
        var ind = super.induceEntropy(entropy, simulate);
        if (!simulate && isOverLoaded()) super.drainEntropy(decay, false);
        return ind;
    }

    @Override
    public int drainEntropy(int entropy, boolean simulate) {
        var drn = super.drainEntropy(entropy, simulate);
        if (!simulate && isOverLoaded()) super.drainEntropy(decay, false);
        return drn;
    }

    @Override
    public boolean isOverLoaded() {
        return (double) getCurrentEntropy() / getMaxEntropy() > getLoadFactor();
    }

    @Override
    public double getLoadFactor() {
        return loadFactor;
    }

    @Override
    public void setLoadFactor(double loadFactor) {
        this.loadFactor = loadFactor;
    }

    @Override
    public int getDecay() {
        return decay;
    }

    @Override
    public void setDecay(int decay) {
        this.decay = decay;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        var tag = super.serializeNBT();
        tag.setInteger("decay", decay);
        tag.setDouble("load_factor", loadFactor);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        decay = nbt.getInteger("decay");
        loadFactor = nbt.getDouble("load_factor");
    }
}
