package io.github.srdjanv.endreforked.api.entropy.world;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk;
import io.github.srdjanv.endreforked.common.capabilities.entropy.CapabilityEntropyHandler;

public final class EntropyWorld {

    private static final Map<Long, EntropyChunk> chunks = new ConcurrentHashMap<>();
    private final World world;

    public EntropyWorld(World world) {
        this.world = world;
    }

    public Optional<EntropyChunk> getEntropyChunk(ChunkPos pos) {
        return getEntropyChunk(ChunkPos.asLong(pos.x, pos.z));
    }

    public Optional<EntropyChunk> getEntropyChunk(BlockPos pos) {
        return getEntropyChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    public Optional<EntropyChunk> getEntropyChunk(int chunkX, int chunkZ) {
        if (world.isAreaLoaded(new BlockPos(chunkX << 4, 0, chunkZ << 4), 0))
            return getEntropyChunk(ChunkPos.asLong(chunkX, chunkZ));
        return Optional.empty();
    }

    private Optional<EntropyChunk> getEntropyChunk(long pos) {
        var data = chunks.get(pos);
        if (data == null) return Optional.empty();
        return Optional.of(data);
    }

    void loadData(Chunk chunk) {
        var data = chunk.getCapability(CapabilityEntropyHandler.ENTROPY_CHUNK, null);
        if (data == null) {
            EndReforked.LOGGER.error("Chunk entropy capability not found {}", chunk.getPos());
            return;
        }
        var put = chunks.put(ChunkPos.asLong(chunk.x, chunk.z), data);
        if (put != null) {
            EndReforked.LOGGER.error("Chunk entropy capability already loaded {}", chunk.getPos());
        }
    }

    void unloadData(Chunk chunk) {
        chunks.remove(ChunkPos.asLong(chunk.x, chunk.z));
    }
}
