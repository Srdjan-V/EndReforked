package io.github.srdjanv.endreforked.api.entropy.storage;

import java.util.Optional;

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage;

public interface EntropyStorageReference {

    Optional<EntropyStorage> getEntropyStorageReference();

    boolean setEntropyStorageReference(EntropyStorage reference, boolean force);
}
