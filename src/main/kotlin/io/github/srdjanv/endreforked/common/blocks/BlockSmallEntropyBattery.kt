package io.github.srdjanv.endreforked.common.blocks

import com.cleanroommc.modularui.factory.TileEntityGuiFactory
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
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

    private fun link(world: World, pos: BlockPos) {
        (world.getTileEntity(pos) as? SmallEntropyBatteryTile)?.linkToChunk(true)
    }
}
