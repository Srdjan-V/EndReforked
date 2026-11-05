package io.github.srdjanv.endreforked.common.capabilities.entropy;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk;
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage;
import io.github.srdjanv.endreforked.api.capabilities.entropy.WeakEntropyStorage;
import io.github.srdjanv.endreforked.common.entropy.storage.DefaultEntropyStorage;
import io.github.srdjanv.endreforked.common.entropy.storage.DefaultWeakEntropyStorage;

public class CapabilityEntropyHandler {

    @CapabilityInject(EntropyStorage.class)
    public static Capability<EntropyStorage> STORAGE;

    @CapabilityInject(WeakEntropyStorage.class)
    public static Capability<WeakEntropyStorage> WEEK_STORAGE;

    @CapabilityInject(EntropyChunk.class)
    public static Capability<EntropyChunk> ENTROPY_CHUNK;
    public static final ResourceLocation ENTROPY_CHUNK_STORAGE_LOC = new ResourceLocation(Tags.MODID,
            "chunk_entropy_storage");

    @SubscribeEvent
    public static void attachChunkEntropyCap(AttachCapabilitiesEvent<Chunk> event) {
        if (!event.getObject().getWorld().isRemote) {
            event.addCapability(ENTROPY_CHUNK_STORAGE_LOC, new ChunkEntropyProvider(event.getObject()));
        }
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(EntropyStorage.class, new Capability.IStorage<>() {

            @Nullable
            @Override
            public NBTBase writeNBT(Capability<EntropyStorage> capability, EntropyStorage instance, EnumFacing side) {
                return new NBTTagInt(instance.getCurrentEntropy());
            }

            @Override
            public void readNBT(Capability<EntropyStorage> capability, EntropyStorage instance, EnumFacing side,
                                NBTBase nbt) {
                if (!(instance instanceof DefaultEntropyStorage defInst))
                    throw new IllegalArgumentException(
                            "Can not deserialize to an instance that isn't the default implementation");
                defInst.setEntropy(((NBTTagInt) nbt).getInt());
            }
        }, () -> new DefaultEntropyStorage(1_000));

        CapabilityManager.INSTANCE.register(WeakEntropyStorage.class, new Capability.IStorage<>() {

            @Nullable
            @Override
            public NBTBase writeNBT(Capability<WeakEntropyStorage> capability, WeakEntropyStorage instance,
                                    EnumFacing side) {
                var tag = new NBTTagCompound();
                tag.setInteger("entropy", instance.getCurrentEntropy());
                tag.setInteger("decay", instance.getDecay());
                return tag;
            }

            @Override
            public void readNBT(Capability<WeakEntropyStorage> capability, WeakEntropyStorage instance, EnumFacing side,
                                NBTBase nbt) {
                if (!(instance instanceof DefaultWeakEntropyStorage defInst))
                    throw new IllegalArgumentException(
                            "Can not deserialize to an instance that isn't the default implementation");
                var tag = (NBTTagCompound) nbt;
                defInst.setEntropy(tag.getInteger("entropy"));
                defInst.setDecay(tag.getInteger("decay"));
            }
        }, () -> new DefaultWeakEntropyStorage(1_000, 10));

        CapabilityManager.INSTANCE.register(EntropyChunk.class, new Capability.IStorage<>() {

            @Nullable
            @Override
            public NBTBase writeNBT(Capability<EntropyChunk> capability, EntropyChunk instance, EnumFacing side) {
                return instance.serializeNBT();
            }

            @Override
            public void readNBT(Capability<EntropyChunk> capability, EntropyChunk instance, EnumFacing side,
                                NBTBase nbt) {
                instance.deserializeNBT((NBTTagCompound) nbt);
            }
        }, () -> null);
    }
}
