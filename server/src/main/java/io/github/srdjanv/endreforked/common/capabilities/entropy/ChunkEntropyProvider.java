package io.github.srdjanv.endreforked.common.capabilities.entropy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.common.entropy.chunks.DefaultEntropyChunk;

public class ChunkEntropyProvider implements ICapabilitySerializable<NBTTagCompound> {

    private final DefaultEntropyChunk instance;

    public ChunkEntropyProvider(Chunk chunk) {
        instance = new DefaultEntropyChunk(chunk);
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEntropyHandler.STORAGE || capability == CapabilityEntropyHandler.WEEK_STORAGE ||
                capability == CapabilityEntropyHandler.ENTROPY_CHUNK;
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEntropyHandler.STORAGE) {
            return CapabilityEntropyHandler.STORAGE.cast(instance);
        } else if (capability == CapabilityEntropyHandler.WEEK_STORAGE) {
            return CapabilityEntropyHandler.WEEK_STORAGE.cast(instance);
        } else if (CapabilityEntropyHandler.ENTROPY_CHUNK != null) {
            return CapabilityEntropyHandler.ENTROPY_CHUNK.cast(instance);
        } else return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        instance.deserializeNBT(nbt);
    }
}
