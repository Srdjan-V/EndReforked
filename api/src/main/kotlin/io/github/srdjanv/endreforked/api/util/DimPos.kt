package io.github.srdjanv.endreforked.api.util

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos

data class DimPos(val dim: Int, val chunkPosX: Int, val chunkPosZ: Int) {
    constructor(dim: Int, pos: BlockPos) : this(dim, pos.x shr 4, pos.z shr 4)
    constructor(dim: Int, pos: ChunkPos) : this(dim, pos.x, pos.z)

    fun asBlockPos() = BlockPos(chunkPosX shl 4, 0, chunkPosZ shl 4)
}
