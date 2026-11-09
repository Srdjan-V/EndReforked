package io.github.srdjanv.endreforked.common.materializer

import net.minecraft.util.math.BlockPos

/**
 * The client state of the ghost preview: the Materializer that the player opened last, and the GUI toggle.
 * It is never saved and never sent. The server does not use it. The renderer is `client/GhostRenderer`.
 */
object GhostPreview {
    @Volatile var enabled = false

    @Volatile var pos: BlockPos? = null
        private set

    fun opened(machine: BlockPos) {
        pos = machine
    }
}
