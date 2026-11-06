package io.github.srdjanv.endreforked.impl.worldgen

import io.github.srdjanv.endreforked.api.worldgen.Spacing
import java.util.Random

/**
 * The vanilla village grid: the world is cut into cells of `spacing` chunks. Each cell has one chunk that gets the structure.
 * The chunk is random in the cell but at least `separation` chunks from the next cell on average. [seeded] returns the random for a cell and the unique id, like `World.setRandomSeed`.
 */
fun Spacing.isStructureChunk(chunkX: Int, chunkZ: Int, seeded: (cellX: Int, cellZ: Int, salt: Int) -> Random): Boolean {
    val cellX = Math.floorDiv(chunkX, spacing)
    val cellZ = Math.floorDiv(chunkZ, spacing)
    val random = seeded(cellX, cellZ, uniqueId)
    val range = spacing - separation
    val x = cellX * spacing + (random.nextInt(range) + random.nextInt(range)) / 2
    val z = cellZ * spacing + (random.nextInt(range) + random.nextInt(range)) / 2
    return chunkX == x && chunkZ == z
}
