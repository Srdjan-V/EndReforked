package io.github.srdjanv.endreforked.common.tiles

import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import net.minecraft.tileentity.TileEntity

/** No tick, no GUI, no data. It only registers the stabilizer in the entropy world while its chunk is loaded. */
class LormyteStabilizerTile : TileEntity() {
    override fun onLoad() {
        if (!world.isRemote) EntropyWorldHandler.entropyWorld(world)?.addStabilizer(pos)
    }

    override fun invalidate() {
        super.invalidate()
        if (world != null && !world.isRemote) EntropyWorldHandler.entropyWorld(world)?.removeStabilizer(pos)
    }

    override fun onChunkUnload() {
        if (!world.isRemote) EntropyWorldHandler.entropyWorld(world)?.removeStabilizer(pos)
    }
}
