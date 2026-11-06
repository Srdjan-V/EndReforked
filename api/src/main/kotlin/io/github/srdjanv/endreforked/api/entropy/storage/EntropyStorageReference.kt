package io.github.srdjanv.endreforked.api.entropy.storage

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage
import java.util.Optional

interface EntropyStorageReference {
    val entropyStorageReference: Optional<EntropyStorage>

    fun setEntropyStorageReference(reference: EntropyStorage?, force: Boolean): Boolean
}
