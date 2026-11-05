package io.github.srdjanv.endreforked.common.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockCrops;

public class BlockOrganaFlowerStem extends BaseBlockCrops {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 9);
    public static final PropertyDirection FACING = BlockTorch.FACING;
    public static final AxisAlignedBB[] STEM_AABB = new AxisAlignedBB[] {
            new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.125D, 0.625D),
            new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.25D, 0.625D),
            new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.375D, 0.625D),
            new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.5D, 0.625D),
            new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.625D, 0.625D),
            new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.75D, 0.625D),
            new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 0.875D, 0.625D),
            new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D) };
    protected static final BlockOrganaFlower FLOWER = ModBlocks.ORGANA_FLOWER_BLOCK.get();

    public BlockOrganaFlowerStem() {
        super("organa_flower_stem");
        setTickRandomly(true);
        sustainableBlocks.add(ModBlocks.END_MOSS_GRASS_BLOCK.get());
        setDefaultState(blockState.getBaseState().withProperty(AGE, 0).withProperty(FACING, EnumFacing.UP));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE, FACING);
    }

    @Override
    protected Item getSeed() {
        return ModItems.ORGANA_FLOWER_SEED.get();
    }

    @Override
    protected Item getCrop() {
        return ModItems.ORGANA_FLOWER.get();
    }

    @Override
    public boolean isMaxAge(IBlockState state) {
        return state.getValue(AGE) >= 7;
    }

    @Override
    public int getMaxAge() {
        return 7;
    }

    public boolean isDead(IBlockState state) {
        return state.getValue(AGE) >= 9;
    }

    @Override
    protected PropertyInteger getAgeProperty() {
        return AGE;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (getActualState(state, source, pos).getValue(FACING) == EnumFacing.UP) {
            return STEM_AABB[Math.min(state.getValue(AGE), getMaxAge())];
        } else return STEM_AABB[4];
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = state.withProperty(FACING, EnumFacing.UP);

        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            if (worldIn.getBlockState(pos.offset(facing)).getBlock() == FLOWER && isMaxAge(state)) {
                state = state.withProperty(FACING, facing);
                break;
            }
        }

        return state;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity te, ItemStack stack) {}

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
                         int fortune) {}

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() == this) // Forge: This function is called during world gen and placement, before this
                                      // block is set, so if we are not 'here' then assume it's the pre-check.
        {
            IBlockState soil = world.getBlockState(pos.down());
            return soil.getBlock().canSustainPlant(soil, world, pos.down(), net.minecraft.util.EnumFacing.UP, this);
        }
        return this.canSustainBush(world.getBlockState(pos.down()));
    }

    @Override
    public void updateTick(World worldIn, BlockPos plantPos, IBlockState state, Random rand) {
        if (worldIn.isRemote) return;
        checkAndDropBlock(worldIn, plantPos, state);

        float f = BlockCrops.getGrowthChance(this, worldIn, plantPos);
        BlockPos fruitPos = plantPos;
        if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, plantPos, state,
                rand.nextInt((int) (25.0F / f) + 1) == 0)) {
            if (!isMaxAge(state)) {
                IBlockState newState = state.withProperty(AGE, state.getValue(AGE) + 1);
                worldIn.setBlockState(plantPos, newState, 2);
                worldIn.playEvent(1033, plantPos, 0);
            } else {
                for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
                    if (worldIn.getBlockState(plantPos.offset(facing)).getBlock() == FLOWER) return;

                fruitPos = plantPos.offset(EnumFacing.Plane.HORIZONTAL.random(rand));
                IBlockState soil = worldIn.getBlockState(fruitPos.down());
                Block soilBlock = soil.getBlock();

                if (worldIn.isAirBlock(fruitPos) &&
                        soilBlock.canSustainPlant(soil, worldIn, fruitPos.down(), EnumFacing.UP, this)) {
                    IBlockState newState = state.withProperty(AGE, state.getValue(AGE) + 1);
                    if (isDead(newState)) {
                        worldIn.setBlockState(plantPos, ModBlocks.ORGANA_FLOWER_STEM_DEAD_BLOCK.get().getDefaultState(),
                                2);
                    } else worldIn.setBlockState(plantPos, newState, 2);
                    worldIn.setBlockState(fruitPos, FLOWER.getDefaultState());
                    worldIn.playEvent(1034, fruitPos, 0);
                }
            }
            net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, fruitPos, state,
                    worldIn.getBlockState(fruitPos));
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public void handleAssets() {
        super.handleAssets();
        EndReforked.getProxy().registerOrganaFlowerStemStateMapper(this);
    }
}
