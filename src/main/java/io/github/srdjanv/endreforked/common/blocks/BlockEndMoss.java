package io.github.srdjanv.endreforked.common.blocks;

import java.util.Random;

import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase;

public class BlockEndMoss extends BlockBase implements IGrowable {

    public BlockEndMoss(String name) {
        super(name, Material.GRASS);
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
                        if (ModBlocks.ORGANA_WEED.get().canPlaceBlockAt(worldIn, pos)) {
                            worldIn.setBlockState(pos, ModBlocks.ORGANA_WEED.get().getDefaultState());
                        } else {
                            var downPos = pos.down();
                            if (worldIn.getBlockState(downPos).getBlock() == Blocks.END_STONE) {
                                worldIn.setBlockState(downPos, getDefaultState());
                            }
                        }
                    }
                    break;
                }

                pos = pos.add(
                        rand.nextInt(3) - 1,
                        (rand.nextInt(3) - 1) * rand.nextInt(3) / 2,
                        rand.nextInt(3) - 1);

                var downPosBlock = worldIn.getBlockState(pos.down()).getBlock();
                if (downPosBlock == this || downPosBlock == Blocks.END_STONE) {
                    ++j;
                } else break;
            }
        }
    }
}