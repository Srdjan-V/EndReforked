package io.github.srdjanv.endreforked.common.capabilities.entropy

import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler.ENTROPY_CHUNK
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler.STORAGE
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler.WEEK_STORAGE
import io.github.srdjanv.endreforked.common.entropy.chunks.DefaultEntropyChunk
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable

class ChunkEntropyProvider(chunk: Chunk) : ICapabilitySerializable<NBTTagCompound> {
    private val instance = DefaultEntropyChunk(chunk)

    override fun hasCapability(capability: Capability<*>, facing: EnumFacing?) =
        capability === STORAGE || capability === WEEK_STORAGE || capability === ENTROPY_CHUNK

    override fun <T> getCapability(capability: Capability<T>, facing: EnumFacing?): T? = when {
        capability === STORAGE -> STORAGE.cast(instance)
        capability === WEEK_STORAGE -> WEEK_STORAGE.cast(instance)
        else -> ENTROPY_CHUNK.cast(instance)
    }

    override fun serializeNBT(): NBTTagCompound = instance.serializeNBT()

    override fun deserializeNBT(nbt: NBTTagCompound) = instance.deserializeNBT(nbt)
}
