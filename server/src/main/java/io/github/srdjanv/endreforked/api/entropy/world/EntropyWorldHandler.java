package io.github.srdjanv.endreforked.api.entropy.world;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk;
import io.github.srdjanv.endreforked.api.util.DimPos;

public final class EntropyWorldHandler {

    private static final Map<Integer, EntropyWorld> worlds = new ConcurrentHashMap<>();

    public static Optional<EntropyChunk> getEntropyChunkWorld(DimPos pos) {
        return getEntropyWorld(pos.dim())
                .flatMap(entropyWorld -> entropyWorld.getEntropyChunk(pos.chunkPosX(), pos.chunkPosZ()));
    }

    public static Optional<EntropyChunk> getEntropyChunkWorld(int dim, ChunkPos pos) {
        return getEntropyWorld(dim).flatMap(entropyWorld -> entropyWorld.getEntropyChunk(pos));
    }

    public static Optional<EntropyChunk> getEntropyChunkWorld(int dim, BlockPos pos) {
        return getEntropyWorld(dim).flatMap(entropyWorld -> entropyWorld.getEntropyChunk(pos));
    }

    public static Optional<EntropyChunk> getEntropyChunkWorld(int dim, int chunkX, int chunkZ) {
        return getEntropyWorld(dim).flatMap(entropyWorld -> entropyWorld.getEntropyChunk(chunkX, chunkZ));
    }

    public static Optional<EntropyWorld> getEntropyWorld(int dim) {
        EntropyWorld entropyWorld = worlds.get(dim);
        if (entropyWorld != null) {
            if (!DimensionManager.isWorldQueuedToUnload(dim)) {
                return Optional.of(entropyWorld);
            } else worlds.remove(dim);
        }
        return Optional.empty();
    }

    @SubscribeEvent
    static void onWorldLoad(WorldEvent.Load event) {
        if (!event.getWorld().isRemote) {
            final var dim = event.getWorld().provider.getDimension();
            worlds.put(dim, new EntropyWorld(event.getWorld()));
        }
    }

    @SubscribeEvent
    static void onWorldUnLoad(WorldEvent.Unload event) {
        final var dim = event.getWorld().provider.getDimension();
        worlds.remove(dim);
    }

    @SubscribeEvent
    static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getWorld().isRemote) {
            Optional<EntropyWorld> optionalEntropyWorld = getEntropyWorld(event.getWorld().provider.getDimension());
            final EntropyWorld world;
            if (optionalEntropyWorld.isPresent()) {
                world = optionalEntropyWorld.get();
            } else {
                EndReforked.LOGGER.warn("Chunk was loaded before world was loaded????");
                final var dim = event.getWorld().provider.getDimension();
                world = worlds.put(dim, new EntropyWorld(event.getWorld()));
            }

            if (world != null) {
                world.loadData(event.getChunk());
            } else EndReforked.LOGGER.error("Cant load chunk data {}", event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    static void onChunkUnLoad(ChunkEvent.Unload event) {
        if (!event.getWorld().isRemote) {
            Optional<EntropyWorld> optionalEntropyWorld = getEntropyWorld(event.getWorld().provider.getDimension());
            final EntropyWorld world;
            if (optionalEntropyWorld.isPresent()) {
                world = optionalEntropyWorld.get();
            } else {
                EndReforked.LOGGER.warn("Chunk was unloaded after world was unloaded????");
                final var dim = event.getWorld().provider.getDimension();
                world = worlds.put(dim, new EntropyWorld(event.getWorld()));
            }

            if (world != null) {
                world.unloadData(event.getChunk());
            } else EndReforked.LOGGER.error("Cant unload chunk data {}", event.getChunk().getPos());
        }
    }
}
