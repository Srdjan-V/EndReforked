package io.github.srdjanv.endreforked.common.blocks

import com.cleanroommc.modularui.factory.TileEntityGuiFactory
import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import io.github.srdjanv.endreforked.common.entropy.CriticalEvents
import io.github.srdjanv.endreforked.common.entropy.Stabilizers
import io.github.srdjanv.endreforked.common.tiles.SmallEntropyBatteryTile
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.WorldServer

class BlockSmallEntropyBattery : BlockBase("small_entropy_battery", Material.IRON) {

    override fun onBlockActivated(
        worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand,
        facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float,
    ): Boolean {
        if (!worldIn.isRemote) TileEntityGuiFactory.open(playerIn, pos)
        return true
    }

    override fun hasTileEntity(state: IBlockState) = true

    override fun createTileEntity(world: World, state: IBlockState): TileEntity = SmallEntropyBatteryTile()

    override fun canConnectRedstone(state: IBlockState, world: IBlockAccess, pos: BlockPos, side: EnumFacing?) = true

    override fun onBlockAdded(worldIn: World, pos: BlockPos, state: IBlockState) {
        if (!worldIn.isRemote && !worldIn.isBlockPowered(pos)) link(worldIn, pos)
    }

    override fun neighborChanged(state: IBlockState, worldIn: World, pos: BlockPos, blockIn: Block, fromPos: BlockPos) {
        if (!worldIn.isRemote && worldIn.isBlockPowered(pos)) link(worldIn, pos)
    }

    /**
     * The battery drops empty. A filled one releases its entropy as one critical event at its position, at any chunk level.
     * The chunk loses the link to the storage that dies with the block.
     */
    override fun breakBlock(world: World, pos: BlockPos, state: IBlockState) {
        val tile = world.getTileEntity(pos) as? SmallEntropyBatteryTile
        if (!world.isRemote && tile != null) {
            val filled = tile.storedEntropy > 0
            tile.unlink()
            if (filled) releaseEntropy(world as WorldServer, pos)
        }
        super.breakBlock(world, pos, state)
    }

    private fun link(world: World, pos: BlockPos) {
        (world.getTileEntity(pos) as? SmallEntropyBatteryTile)?.linkToChunk(true)
    }

    companion object {
        /** Runs one critical event at [pos], unless a Lormyte Stabilizer absorbs it (Organa absorption). */
        fun releaseEntropy(world: WorldServer, pos: BlockPos) {
            if (Stabilizers.absorb(world, pos)) return
            val events = EndReforked.getProxy().registries(world).criticalEvents.current.entries
            val event = CriticalEvents.pick(events, world.rand) ?: return
            CriticalEvents.run(world, event, pos)
        }
    }
}
