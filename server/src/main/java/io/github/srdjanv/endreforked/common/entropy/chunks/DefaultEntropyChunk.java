package io.github.srdjanv.endreforked.common.entropy.chunks;

import java.util.Optional;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk;
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyStorage;
import io.github.srdjanv.endreforked.api.util.DimPos;
import io.github.srdjanv.endreforked.common.entropy.storage.DefaultEntropyStorageReference;
import io.github.srdjanv.endreforked.common.entropy.storage.DefaultWeakEntropyStorage;

public class DefaultEntropyChunk implements EntropyChunk {

    private final World world;
    private final DimPos dimPos;
    private final DefaultWeakEntropyStorage storage;
    private final DefaultEntropyStorageReference storageReference;
    private int tickValidated;
    private boolean isLoaded = true;

    public DefaultEntropyChunk(Chunk chunk) {
        world = chunk.getWorld();
        this.dimPos = new DimPos(world.provider.getDimension(), chunk.getPos());
        var rand = chunk.getWorld().rand;
        storage = new DefaultWeakEntropyStorage(
                Math.max(1000, rand.nextInt(1200)),
                Math.max(5, rand.nextInt(10)));
        storageReference = new DefaultEntropyStorageReference();
    }

    @Override
    public Optional<EntropyStorage> getEntropyStorageReference() {
        return storageReference.getEntropyStorageReference();
    }

    @Override
    public boolean setEntropyStorageReference(EntropyStorage reference, boolean force) {
        return storageReference.setEntropyStorageReference(reference, force);
    }

    @Override
    public boolean isLoaded() {
        var counter = FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter();
        if (counter != tickValidated) {
            tickValidated = counter;
            return isLoaded = world.isAreaLoaded(dimPos.asBlockPos(), 0);
        }
        return isLoaded;
    }

    @Override
    public DimPos getDimPos() {
        return dimPos;
    }

    @Override
    public double getLoadFactor() {
        return storage.getLoadFactor();
    }

    @Override
    public void setLoadFactor(double loadFactor) {
        storage.setLoadFactor(loadFactor);
    }

    @Override
    public boolean isOverLoaded() {
        return storage.isOverLoaded();
    }

    @Override
    public int getDecay() {
        return storage.getDecay();
    }

    @Override
    public void setDecay(int decay) {
        storage.setDecay(decay);
    }

    @Override
    public int getMaxEntropy() {
        return storage.getMaxEntropy();
    }

    @Override
    public int getCurrentEntropy() {
        return storage.getCurrentEntropy();
    }

    @Override
    public int induceEntropy(int entropy, boolean simulate) {
        int ind = storage.induceEntropy(entropy, simulate);
        var ref = storageReference.getEntropyStorageReference();
        if (ref.isPresent() && entropy > ind) {
            ind += ref.get().induceEntropy(entropy - ind, simulate);
        }
        return ind;
    }

    @Override
    public int drainEntropy(int entropy, boolean simulate) {
        int drain = storage.drainEntropy(entropy, simulate);
        var ref = storageReference.getEntropyStorageReference();
        if (ref.isPresent() && entropy > drain) {
            drain += ref.get().drainEntropy(entropy - drain, simulate);
        }
        return drain;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return storage.serializeNBT();
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        storage.deserializeNBT(nbt);
    }

    @Override
    public Optional<EntropyStorage> getEntropyStorage() {
        return Optional.of(storage);
    }
}
