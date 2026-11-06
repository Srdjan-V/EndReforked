package io.github.srdjanv.endreforked.api.entropy;

import java.util.List;

import io.github.srdjanv.endreforked.api.util.DimPos;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public enum EntropyRadius {

    ONE(0),
    TWO(1),
    THREE(2),
    FOR(3);

    private final int radius;
    private final int chunks;

    EntropyRadius(int radius) {
        this.radius = radius;
        this.chunks = chunksInRadius(radius);
    }

    public int getChunks() {
        return chunks;
    }

    public int getRadius() {
        return radius;
    }

    public List<DimPos> getChunksInRadius(DimPos center) {
        List<DimPos> chunks = new ObjectArrayList<>();

        for (int x = center.chunkPosX() - radius; x <= center.chunkPosX() + radius; x++) {
            for (int z = center.chunkPosZ() - radius; z <= center.chunkPosZ() + radius; z++) {
                chunks.add(new DimPos(center.dim(), x, z));
            }
        }
        return chunks;
    }

    private static int chunksInRadius(int radius) {
        int chunks = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                chunks++;
            }
        }
        return chunks;
    }
}
