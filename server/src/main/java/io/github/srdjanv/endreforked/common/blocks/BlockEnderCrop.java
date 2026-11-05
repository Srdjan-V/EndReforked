package io.github.srdjanv.endreforked.common.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockCrops;

public class BlockEnderCrop extends BaseBlockCrops {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

    public BlockEnderCrop() {
        super("ender_flower_crop");
        sustainableBlocks.add(Blocks.END_BRICKS);
        sustainableBlocks.add(Blocks.END_STONE);
        sustainableBlocks.add(ModBlocks.ENTROPY_END_STONE.get());
        sustainableBlocks.add(ModBlocks.ENTROPY_CROP_BLOCK.get());
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }

    @Override
    protected PropertyInteger getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) return false;
        if (this.isMaxAge(state)) {
            worldIn.spawnEntity(
                    new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(),
                            new ItemStack(Items.ENDER_PEARL, worldIn.rand.nextInt(3))));
            worldIn.setBlockState(pos, ModBlocks.ENDER_FLOWER_CROP_DEAD.get().getDefaultState());
            return true;
        }
        return false;
    }

    @Override
    protected Item getSeed() {
        return ModItems.ENDER_FLOWER_CROP.get();
    }

    @Override
    protected Item getCrop() {
        return Items.ENDER_PEARL;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
                             @Nullable TileEntity te, ItemStack stack) {
        if (worldIn.isRemote) return;
        if (!isMaxAge(state)) return;
        worldIn.spawnEntity(
                new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(),
                        new ItemStack(Items.ENDER_PEARL, worldIn.rand.nextInt(3))));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
                         int fortune) {
        if (getAge(state) >= getMaxAge()) drops.add(new ItemStack(getCrop()));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return isMaxAge(state) ? getCrop() : Items.AIR;
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
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
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) return;
        checkAndDropBlock(worldIn, pos, state);

        // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (!worldIn.isAreaLoaded(pos, 1)) return;

        int i = getAge(state);
        if (i < getMaxAge()) {
            float f = getGrowthChance(this, worldIn, pos);

            if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state,
                    rand.nextInt((int) (2.0F / f) + 1) == 0)) {
                worldIn.setBlockState(pos, withAge(i + 1), 2);
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state,
                        worldIn.getBlockState(pos));
            }
        }
    }
}
