package io.github.srdjanv.endreforked.common.blocks;

import java.util.Collections;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.IGrowable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockBush;

public class BlockEndCoral extends BaseBlockBush implements IShearable, IGrowable {

    public static final AxisAlignedBB END_BUSH_AABB = new AxisAlignedBB(0.09999999403953552D, 0.0D,
            0.09999999403953552D, 0.8999999761581421D, 0.400000011920929D, 0.8999999761581421D);

    public BlockEndCoral() {
        super("end_coral", Material.VINE);
        sustainableBlocks.add(Blocks.END_BRICKS);
        sustainableBlocks.add(Blocks.END_STONE);
        sustainableBlocks.add(ModBlocks.ENTROPY_END_STONE.get());
        sustainableBlocks.add(ModBlocks.ENTROPY_CROP_BLOCK.get());
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return END_BUSH_AABB;
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.YELLOW;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity te, ItemStack stack) {}

    @Override
    public boolean isShearable(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public java.util.List<ItemStack> onSheared(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos,
                                               int fortune) {
        return Collections.singletonList(new ItemStack(ModBlocks.END_CORAL.get()));
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos startPos, IBlockState state) {
        BlockPos upPos = startPos.up();

        for (int i = 0; i < 64; ++i) {
            BlockPos pos = upPos;
            int j = 0;

            while (true) {
                if (j >= i / 16) {
                    if (worldIn.isAirBlock(pos)) {
                        if (canPlaceBlockAt(worldIn, pos)) worldIn.setBlockState(pos, getDefaultState());
                    }
                    break;
                }

                pos = pos.add(
                        rand.nextInt(3) - 1,
                        (rand.nextInt(3) - 1) * rand.nextInt(3) / 2,
                        rand.nextInt(3) - 1);

                if (worldIn.getBlockState(pos.down()).getBlock() != Blocks.END_STONE ||
                        worldIn.getBlockState(pos).isNormalCube()) {
                    break;
                }

                ++j;
            }
        }
    }
}
