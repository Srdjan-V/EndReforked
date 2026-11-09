package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.Random

/**
 * Organa soil that gives a redstone signal of 15 when the Organa plant on it has finished growing.
 * It checks the plant every [CHECK_INTERVAL] ticks.
 */
class BlockOrganaSensorSoil : BlockBase("organa_sensor_soil", Material.ROCK, MapColor.PURPLE) {
    init {
        setSoundType(SoundType.SLIME)
        setHarvestLevel("pickaxe", 2)
        setHardness(1F)
        defaultState = blockState.baseState.withProperty(POWERED, false)
    }

    override fun createBlockState() = BlockStateContainer(this, POWERED)

    override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(POWERED, meta == 1)

    override fun getMetaFromState(state: IBlockState) = if (state.getValue(POWERED)) 1 else 0

    override fun canProvidePower(state: IBlockState) = true

    override fun getWeakPower(state: IBlockState, world: IBlockAccess, pos: BlockPos, side: EnumFacing) =
        if (state.getValue(POWERED)) 15 else 0

    override fun onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState) {
        if (!worldIn.isRemote) worldIn.scheduleUpdate(pos, this, CHECK_INTERVAL)
    }

    override fun updateTick(worldIn: World, pos: BlockPos, state: IBlockState, rand: Random) {
        if (worldIn.isRemote) return
        val finished = isFinished(pos.up()) { x, y, z -> cellAt(worldIn, BlockPos(x, y, z)) }
        if (finished != state.getValue(POWERED)) {
            worldIn.setBlockState(pos, state.withProperty(POWERED, finished), 3)
            worldIn.notifyNeighborsOfStateChange(pos, this, false)
        }
        worldIn.scheduleUpdate(pos, this, CHECK_INTERVAL)
    }

    private fun cellAt(world: World, pos: BlockPos): Int {
        if (!world.isBlockLoaded(pos)) return NOT_PLANT
        val state = world.getBlockState(pos)
        return when (state.block) {
            ModBlocks.ORGANA_PLANT_BLOCK -> PLANT
            ModBlocks.ORGANA_FLOWER_BLOCK -> state.getValue(BlockOrganaFlower.AGE)
            else -> NOT_PLANT
        }
    }

    companion object {
        @JvmField
        val POWERED: PropertyBool = PropertyBool.create("powered")

        const val CHECK_INTERVAL = 40
        const val MAX_DEPTH = 9

        /** Cell value for a block that is not part of the plant. */
        const val NOT_PLANT = -2

        /** Cell value for an Organa plant block. A flower cell is its age (0 or more). */
        const val PLANT = -1

        /** A flower with an age below this value still grows. */
        const val MATURE_AGE = 8

        /**
         * True when the plant from [start] has finished growing. Follows only connected plant and flower cells
         * (6 directions) to [MAX_DEPTH] blocks from [start]. [cell] gives [NOT_PLANT], [PLANT] or a flower age.
         * It needs at least one cell, and no flower with an age below [MATURE_AGE].
         */
        fun isFinished(start: BlockPos, cell: (Int, Int, Int) -> Int): Boolean {
            val seen = HashSet<Triple<Int, Int, Int>>()
            var queue = listOf(Triple(start.x, start.y, start.z))
            seen.addAll(queue)
            var found = false
            for (depth in 0..MAX_DEPTH) {
                val next = ArrayList<Triple<Int, Int, Int>>()
                for ((x, y, z) in queue) {
                    val c = cell(x, y, z)
                    if (c == NOT_PLANT) continue
                    if (c in 0 until MATURE_AGE) return false
                    found = true
                    for (f in EnumFacing.values()) {
                        val n = Triple(x + f.xOffset, y + f.yOffset, z + f.zOffset)
                        if (seen.add(n)) next.add(n)
                    }
                }
                queue = next
            }
            return found
        }
    }
}
