package io.github.srdjanv.endreforked.api.capabilities.entropy

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.entropy.storage.EntropyStorageReference
import io.github.srdjanv.endreforked.api.util.DimPos
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

interface EntropyChunk : INBTSerializable<NBTTagCompound>, WeakEntropyStorage, EntropyStorageReference, EntropyDataProvider {
    val isLoaded: Boolean
    val dimPos: DimPos
}
