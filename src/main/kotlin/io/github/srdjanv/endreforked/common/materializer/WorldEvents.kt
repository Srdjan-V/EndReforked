package io.github.srdjanv.endreforked.common.materializer

import com.google.common.base.Predicate
import io.github.srdjanv.endreforked.api.machine.StructureSpec
import io.github.srdjanv.endreforked.api.machine.WorldAction
import io.github.srdjanv.endreforked.api.machine.WorldEvent
import net.minecraft.block.Block
import net.minecraft.block.state.BlockWorldState
import net.minecraft.block.state.IBlockState
import net.minecraft.block.state.pattern.BlockPattern
import net.minecraft.block.state.pattern.FactoryBlockPattern
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/** Runs the world events of the Materializer. */
object WorldEvents {
    /** The search area around the machine: this many blocks to each side (x and z), and from the machine level up this many blocks. */
    const val AREA_RADIUS = 3
    const val AREA_HEIGHT = 5

    /** True when the offset from the machine is in the search area and not in the column of the machine. */
    fun inArea(dx: Int, dy: Int, dz: Int) =
        abs(dx) <= AREA_RADIUS && abs(dz) <= AREA_RADIUS && dy in 0..AREA_HEIGHT && !(dx == 0 && dz == 0)

    /**
     * The default place of a structure for the ghost preview: the block of aisle [aisle], row [row] and column [column].
     * The structure starts above the machine and one block in front of it (the column of the machine is not searched).
     * The aisles go up, the rows go away from the machine, and the columns go to the right of its [facing].
     */
    fun ghostPos(machine: BlockPos, facing: EnumFacing, aisle: Int, row: Int, column: Int): BlockPos {
        val right = facing.rotateY()
        return machine.up().offset(facing).offset(right, column).up(aisle).offset(facing, row)
    }

    /** The states of a structure for the GUI preview, indexed [aisle][row][column]. A blank is air. */
    class Compiled(val states: Array<Array<Array<IBlockState>>>, val pattern: BlockPattern)

    // ponytail: never evicted. Structures are small and few. Evict if scripts make thousands of them.
    private val cache = ConcurrentHashMap<StructureSpec, Compiled>()

    fun compile(spec: StructureSpec): Compiled = cache.getOrPut(spec) {
        val air = Blocks.AIR.defaultState
        val states = spec.symbols
        val factory = FactoryBlockPattern.start()
        spec.aisles.forEach { factory.aisle(*it.toTypedArray()) }
        states.forEach { (symbol, state) -> factory.where(symbol, Predicate<BlockWorldState> { it != null && it.blockState == state }) }
        Compiled(
            Array(spec.aisles.size) { a -> Array(spec.aisles[a].size) { r -> Array(spec.aisles[a][r].length) { c -> states[spec.aisles[a][r][c]] ?: air } } },
            factory.build(),
        )
    }

    /**
     * Looks for the structure of [event] near [center] and runs the action on the first match. Returns false when there is none.
     * The start blocks of the area ([inArea]) are searched in random order. A match must lie fully in the area.
     */
    fun run(world: WorldServer, center: BlockPos, event: WorldEvent): Boolean {
        val compiled = compile(event.structure)
        val box = BlockPos.getAllInBox(center.add(-AREA_RADIUS, 0, -AREA_RADIUS), center.add(AREA_RADIUS, AREA_HEIGHT, AREA_RADIUS))
            .map { it.toImmutable() }
        for (start in box.shuffled(world.rand)) {
            if (!inArea(start.x - center.x, start.y - center.y, start.z - center.z)) continue
            if (!world.isBlockLoaded(start)) continue
            val match = compiled.pattern.match(world, start) ?: continue
            val blocks = ArrayList<BlockPos>()
            for ((a, aisle) in event.structure.aisles.withIndex()) {
                for ((r, row) in aisle.withIndex()) {
                    for ((c, symbol) in row.withIndex()) {
                        if (symbol != StructureSpec.ANY) blocks += match.translateOffset(c, r, a).pos
                    }
                }
            }
            if (blocks.any { !inArea(it.x - center.x, it.y - center.y, it.z - center.z) }) continue
            blocks.forEach { apply(world, it, event.action) }
            return true
        }
        return false
    }

    private fun apply(world: WorldServer, pos: BlockPos, action: WorldAction) {
        if (world.isAirBlock(pos)) return
        particles(world, pos)
        when (action) {
            is WorldAction.SetBlock -> world.setBlockState(pos, action.block)
            is WorldAction.DropItem -> {
                world.setBlockToAir(pos)
                Block.spawnAsEntity(world, pos, action.item.copy())
            }
        }
    }

    /** Crit particles inside the bounding box of the block, 0.3 apart. */
    private fun particles(world: WorldServer, pos: BlockPos) {
        val box = world.getBlockState(pos).getBoundingBox(world, pos)
        fun steps(from: Double, to: Double) = generateSequence(from) { it + 0.3 }.takeWhile { it < to }
        for (x in steps(pos.x + box.minX, pos.x + box.maxX)) {
            for (y in steps(pos.y + box.minY, pos.y + box.maxY)) {
                for (z in steps(pos.z + box.minZ, pos.z + box.maxZ)) {
                    world.spawnParticle(EnumParticleTypes.CRIT, x, y, z, 1, 0.0, 0.0, 0.0, 0.2)
                }
            }
        }
    }
}
