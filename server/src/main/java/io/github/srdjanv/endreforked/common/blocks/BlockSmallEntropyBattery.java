package io.github.srdjanv.endreforked.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.factory.TileEntityGuiFactory;

import io.github.srdjanv.endreforked.common.blocks.base.BlockBase;
import io.github.srdjanv.endreforked.common.tiles.SmallEntropyBatteryTile;

public class BlockSmallEntropyBattery extends BlockBase {

    public BlockSmallEntropyBattery() {
        super("small_entropy_battery", Material.IRON);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) TileEntityGuiFactory.open(playerIn, pos);
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new SmallEntropyBatteryTile();
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side) {
        return true;
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (worldIn.isRemote) return;
        if (!worldIn.isBlockPowered(pos)) {
            var tile = worldIn.getTileEntity(pos);
            if (tile instanceof SmallEntropyBatteryTile battery) {
                battery.linkToChunk(true);
            }
        }
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (worldIn.isRemote) return;
        if (worldIn.isBlockPowered(pos)) {
            var tile = worldIn.getTileEntity(pos);
            if (tile instanceof SmallEntropyBatteryTile battery) {
                battery.linkToChunk(true);
            }
        }
    }
}
