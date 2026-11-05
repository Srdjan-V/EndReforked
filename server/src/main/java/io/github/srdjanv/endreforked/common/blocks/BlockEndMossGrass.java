package io.github.srdjanv.endreforked.common.blocks;

import java.util.Random;

import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase;

public class BlockEndMossGrass extends BlockBase implements IGrowable {

    public BlockEndMossGrass() {
        super("end_moss_grass", Material.GRASS, MapColor.PURPLE);
        setSoundType(SoundType.GLASS);
        setHarvestLevel("pickaxe", 2);
        setHardness(1);
        setTickRandomly(true);
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if (entityIn.isSneaking()) {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
        } else entityIn.fall(fallDistance, 0.0F);
    }

    @Override
    public void onLanded(World worldIn, Entity entityIn) {
        if (entityIn.isSneaking()) {
            super.onLanded(worldIn, entityIn);
        } else if (entityIn.motionY < 0.0D) {
            if (entityIn.motionY > -0.6D) {
                super.onLanded(worldIn, entityIn);
                return;
            }

            entityIn.motionY = -entityIn.motionY;
            if (!(entityIn instanceof EntityLivingBase)) {
                entityIn.motionY *= 0.8D;
            }
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote) return;
        if (!worldIn.isAreaLoaded(pos, 3))
            return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
        spreadMoss(worldIn, pos, rand);
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
        spreadMoss(worldIn, startPos, rand);
        BlockPos upPos = startPos.up();
        for (int i = 0; i < 64; ++i) {
            BlockPos pos = upPos;
            int j = 0;

            while (true) {
                if (j >= i / 16) {
                    if (worldIn.isAirBlock(pos)) {
                        if (ModBlocks.ORGANA_WEED_BLOCK.get().canPlaceBlockAt(worldIn, pos)) {
                            worldIn.setBlockState(pos, ModBlocks.ORGANA_WEED_BLOCK.get().getDefaultState());
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

                // worldIn.getBlockState(blockpos1.down()).getBlock() != Blocks.GRASS ||
                if (downPosBlock == this || !worldIn.getBlockState(pos).isNormalCube()) {
                    ++j;
                } else break;
            }
        }
    }

    private void spreadMoss(World worldIn, BlockPos pos, Random rand) {
        if (worldIn.getLightFromNeighbors(pos.up()) < 4 &&
                worldIn.getBlockState(pos.up()).getLightOpacity(worldIn, pos.up()) > 2) {
            worldIn.setBlockState(pos, ModBlocks.END_MOSS_BLOCK.get().getDefaultState());
        }
        for (int i = 0; i < 4; ++i) {
            BlockPos randPos = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);
            if (randPos.getY() >= 0 && randPos.getY() < 256 && !worldIn.isBlockLoaded(randPos)) return;

            IBlockState randPosStateUp = worldIn.getBlockState(randPos.up());
            IBlockState randPosState = worldIn.getBlockState(randPos);

            if (randPosState.getBlock() == ModBlocks.END_MOSS_BLOCK.get() &&
                    worldIn.getLightFromNeighbors(randPos.up()) >= 4 &&
                    randPosStateUp.getLightOpacity(worldIn, pos.up()) <= 2) {
                worldIn.setBlockState(randPos, this.getDefaultState());
            }
        }
    }
}
