package io.github.srdjanv.endreforked.common.entropy.world

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.api.entropy.world.ChunkEntropyView
import io.github.srdjanv.endreforked.api.util.DimPos
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Optional
import java.util.function.Function
import java.util.function.Supplier

class EntropyChunkReader(
    centerPosSup: Supplier<DimPos?>,
    radius: EntropyRadius = EntropyRadius.ONE,
) {
    private var view = newView(centerPosSup, radius)

    val radius: EntropyRadius
        get() = view.entropyRadius.orElseThrow { IllegalStateException("Entropy radius is null") }

    val entropyView: ChunkEntropyView
        get() = view.also { it.buildView() }

    val centerEntropy: EntropyChunk?
        get() = entropyView.centerEntropy

    fun updateRadius(radius: EntropyRadius) {
        if (this.radius != radius) view = newView(view.centerPosSup, radius)
    }

    companion object {
        private fun newView(centerPosSup: Supplier<DimPos?>, radius: EntropyRadius) =
            ChunkEntropyView(centerPosSup, Function<DimPos, Optional<EntropyChunk>> { EntropyWorldHandler.getEntropyChunkWorld(it) }, radius)

        private fun dimPos(world: World?, pos: BlockPos) = world?.let { DimPos(it.provider.dimension, pos) }

        fun ofEntity(entity: Entity, radius: EntropyRadius = EntropyRadius.ONE) =
            EntropyChunkReader({ dimPos(entity.world, entity.position) }, radius)

        fun ofTileEntity(tileEntity: TileEntity, radius: EntropyRadius = EntropyRadius.ONE) =
            EntropyChunkReader({ dimPos(tileEntity.world, tileEntity.pos) }, radius)
    }
}
