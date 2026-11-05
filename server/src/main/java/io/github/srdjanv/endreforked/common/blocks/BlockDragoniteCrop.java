package io.github.srdjanv.endreforked.common.blocks;

import java.util.Random;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockCrops;

public class BlockDragoniteCrop extends BaseBlockCrops {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 4);
    public static final AxisAlignedBB[] CROP_AABB = new AxisAlignedBB[] {
            new AxisAlignedBB(0.2, 0.0, 0.2, 0.8, 0.55, 0.8),
            new AxisAlignedBB(0.2, 0.0, 0.2, 0.8, 0.70, 0.8),
            new AxisAlignedBB(0.2, 0.0, 0.2, 0.8, 0.85, 0.8),
            new AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 0.90, 0.9),
            new AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 0.95, 0.9) };

    public BlockDragoniteCrop() {
        super("dragonite_crop");
        sustainableBlocks.add(Blocks.END_BRICKS);
        sustainableBlocks.add(Blocks.END_STONE);
        sustainableBlocks.add(ModBlocks.ENTROPY_END_STONE.get());
        sustainableBlocks.add(ModBlocks.ENTROPY_CROP_BLOCK.get());
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        checkAndDropBlock(worldIn, pos, state);
        int i = this.getAge(state);
        if (i >= this.getMaxAge()) return;

        float f = getGrowthChance(this, worldIn, pos);
        if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state,
                rand.nextInt((int) (25.0F / f) + 1) == 0)) {
            worldIn.setBlockState(pos, this.withAge(i + 1), 2);
            net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return CROP_AABB[state.getValue(getAgeProperty())];
    }

    @Override
    protected PropertyInteger getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 4;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return handleRightClick(worldIn, pos, state, () -> new ItemStack(ModItems.DRAGONITE_BERRIES.get(), 2));
    }

    @Override
    protected Item getSeed() {
        return ModItems.DRAGONITE_BERRIES.get();
    }

    @Override
    protected Item getCrop() {
        return ModItems.DRAGONITE_BERRIES.get();
    }
}
