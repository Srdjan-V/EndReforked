package io.github.srdjanv.endreforked.common.entropy.storage;

import java.util.Optional;

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage;
import io.github.srdjanv.endreforked.api.entropy.storage.EntropyStorageReference;

public class DefaultEntropyStorageReference implements EntropyStorageReference {

    private EntropyStorage reference;

    @Override
    public Optional<EntropyStorage> getEntropyStorageReference() {
        return Optional.ofNullable(reference);
    }

    @Override
    public boolean setEntropyStorageReference(EntropyStorage reference, boolean force) {
        if (reference != null && this.reference == reference) return true;
        if (this.reference == null || force) {
            this.reference = reference;
            return true;
        }
        return false;
    }
}
