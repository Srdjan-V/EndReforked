package io.github.srdjanv.endreforked.api.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record DimPos(int dim, int chunkPosX, int chunkPosZ) {

    public DimPos(int dim, BlockPos pos) {
        this(dim, pos.getX() >> 4, pos.getZ() >> 4);
    }

    public DimPos(int dim, ChunkPos pos) {
        this(dim, pos.x, pos.z);
    }

    public static long chunkPosAsLong(int x, int z) {
        return ChunkPos.asLong(x, z);
    }

    public ChunkPos asChunkPos() {
        return new ChunkPos(chunkPosX, chunkPosZ);
    }

    public BlockPos asBlockPos() {
        return new BlockPos(chunkPosX << 4, 0, chunkPosZ << 4);
    }
}
