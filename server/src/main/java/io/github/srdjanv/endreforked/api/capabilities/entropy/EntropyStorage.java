package io.github.srdjanv.endreforked.api.capabilities.entropy;

public interface EntropyStorage {

    int getMaxEntropy();

    int getCurrentEntropy();

    /**
     * Used to determine if this storage can receive entropy.
     * If this is false, then any calls to induceEntropy will return 0.
     */
    default boolean canInduceEntropy() {
        return getCurrentEntropy() < getMaxEntropy();
    }

    /**
     * @param entropy  Entropy amount to induce.
     * @param simulate If false, the fill will only be simulated.
     * @return Amount of entropy that was accepted by the storage.
     */
    int induceEntropy(int entropy, boolean simulate);

    /**
     * Returns if this storage can have entropy extracted.
     * If this is false, then any calls to drainEntropy will return 0.
     */
    default boolean canDrainEntropy() {
        return getCurrentEntropy() != 0;
    }

    /**
     * Drains entropy
     *
     * @param entropy  Maximum amount of entropy to drain.
     * @param simulate If false, drain will only be simulated.
     * @return The amount that was (or would have been, if
     *         simulated) drained.
     */
    int drainEntropy(int entropy, boolean simulate);
}
