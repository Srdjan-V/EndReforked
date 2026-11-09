package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.blocks.BlockOrganaSensorSoil.Companion.NOT_PLANT
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaSensorSoil.Companion.PLANT
import net.minecraft.util.math.BlockPos
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OrganaSensorSoilTest {
    private val start = BlockPos(0, 1, 0)

    private fun finished(grid: Map<Triple<Int, Int, Int>, Int>) =
        BlockOrganaSensorSoil.isFinished(start) { x, y, z -> grid[Triple(x, y, z)] ?: NOT_PLANT }

    /** A vertical stack of plant blocks with [top] on the end. */
    private fun stack(height: Int, top: Int) =
        (0 until height).associate { Triple(0, 1 + it, 0) to PLANT } + (Triple(0, 1 + height, 0) to top)

    @Test
    fun emptyIsNotFinished() = assertFalse(finished(emptyMap()))

    @Test
    fun growingFlowerIsNotFinished() = assertFalse(finished(stack(2, 7)))

    @Test
    fun matureFlowerIsFinished() {
        assertTrue(finished(stack(2, 8)))
        assertTrue(finished(stack(2, 10)))
    }

    @Test
    fun plantOnlyIsFinished() = assertTrue(finished(mapOf(Triple(0, 1, 0) to PLANT)))

    @Test
    fun anyGrowingBranchBlocks() {
        val grid = stack(2, 10) + (Triple(1, 2, 0) to PLANT) + (Triple(2, 2, 0) to 3)
        assertFalse(finished(grid))
    }

    @Test
    fun disconnectedFlowerIsIgnored() = assertTrue(finished(stack(2, 10) + (Triple(5, 2, 5) to 3)))

    @Test
    fun depthLimitIsNine() {
        // Cells at depth 0..9 count: the growing flower at depth 9 blocks, at depth 10 it is not seen.
        assertFalse(finished(stack(9, 3)))
        assertTrue(finished(stack(10, 3)))
    }
}
