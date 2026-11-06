package io.github.srdjanv.endreforked.api.capabilities.entropy;

public interface WeakEntropyStorage extends EntropyStorage {

    double getLoadFactor();

    void setLoadFactor(double loadFactor);

    boolean isOverLoaded();

    /**
     * The amount of entropy lost for each inversion/extraction if the storage exceeds the load factor
     */
    int getDecay();

    void setDecay(int decay);
}
