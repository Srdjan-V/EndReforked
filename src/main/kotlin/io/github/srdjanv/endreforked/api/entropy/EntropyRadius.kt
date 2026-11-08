package io.github.srdjanv.endreforked.api.entropy

import io.github.srdjanv.endreforked.api.util.DimPos

enum class EntropyRadius(val radius: Int) {
    ONE(0),
    TWO(1),
    THREE(2),
    FOR(3);

    val chunks = (2 * radius + 1) * (2 * radius + 1)

    fun getChunksInRadius(center: DimPos): List<DimPos> = buildList {
        for (x in center.chunkPosX - radius..center.chunkPosX + radius)
            for (z in center.chunkPosZ - radius..center.chunkPosZ + radius)
                add(DimPos(center.dim, x, z))
    }
}
