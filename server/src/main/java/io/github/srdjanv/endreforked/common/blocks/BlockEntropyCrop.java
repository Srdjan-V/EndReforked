package io.github.srdjanv.endreforked.common.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.common.blocks.base.BaseBlockCrops;
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.EntropyCropTile;

public class BlockEntropyCrop extends BaseBlockCrops {

    public static final EnumPlantType XORCITE = EnumPlantType.getPlantType("xorcite");
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
    protected static final AxisAlignedBB ESSENCE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    public BlockEntropyCrop() {
        super("entropy_crop");
        setSoundType(SoundType.STONE);
        setHardness(3.0F);
        setHarvestLevel("pickaxe", 2);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return switch (state.getValue(AGE)) {
            default -> 0;
            case 2 -> 1;
            case 3 -> 2;
        };
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return state.equals(this.getDefaultState().withProperty(AGE, 3));
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return state.equals(this.getDefaultState().withProperty(AGE, 3)) ? new EntropyCropTile() : null;
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(ModItems.ENTROPY_CROP_BLOCK.get(), 1, 0);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(ModItems.ENTROPY_CROP_BLOCK.get(), 1, 0));
        items.add(new ItemStack(ModItems.ENTROPY_CROP_BLOCK.get(), 1, 3));
    }

    @Override
    protected Item getSeed() {
        return ModItems.ENTROPY_CROP_BLOCK.get();
    }

    @Override
    protected Item getCrop() {
        return ModItems.ENTROPY_SHARD.get();
    }

    @Override
    public int getMaxAge() {
        return 3;
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
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return false;
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return XORCITE;
    }

    @Override
    public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
        if (world == null || pos == null) return getDefaultState();
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) return getDefaultState();
        return state;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (isMaxAge(state)) return;
        if (!worldIn.isAreaLoaded(pos, 1)) return;

        // TODO: 17/11/2023 add config options
        int existingXBlock = 0;
        var searchArea = BlockPos.getAllInBox(pos.add(2, 2, 2), pos.add(-2, -2, -2));
        for (BlockPos p : searchArea) {
            if (existingXBlock >= 6) break; // break early
            if (worldIn.getBlockState(p).getBlock() == ModBlocks.ENTROPY_CROP_BLOCK.get()) existingXBlock++;
        }

        if (existingXBlock < 6) {
            var newArea = BlockPos.getAllInBox(pos.add(1, 1, 1), pos.add(-1, -1, -1));
            List<BlockPos> validPos = new ArrayList<>();
            for (BlockPos blockPos : newArea)
                if (worldIn.getBlockState(blockPos).getBlock() == Blocks.END_STONE) validPos.add(blockPos);

            Collections.shuffle(validPos);
            for (int j = 0; j < Math.min(validPos.size(), 3); j++)
                worldIn.setBlockState(validPos.get(j), this.getDefaultState());
        }

        if (rand.nextInt(worldIn.provider.getDimension() == 1 ? 2 : 10) != 0) return;
        grow(worldIn, pos, state);
    }

    @Override
    public void grow(World worldIn, BlockPos pos, IBlockState state) {
        int i = this.getAge(state);
        if (i >= 3) return;
        worldIn.setBlockState(pos, this.withAge(i + 1), 2);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
                         int fortune) {
        if (isMaxAge(state))
            addAdditionalDrops(world, pos, fortune, drops, world instanceof World ? ((World) world).rand : RANDOM);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        // do not drop items while restoring blockstates, prevents item dupe
        if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) {
            // use the old method until it gets removed, for backward compatibility
            List<ItemStack> drops = getDrops(worldIn, pos, state, fortune);
            chance = net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, worldIn, pos, state, fortune,
                    chance, false, harvesters.get());

            for (ItemStack drop : drops)
                if (worldIn.rand.nextFloat() <= chance) spawnAsEntity(worldIn, pos, drop);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!isMaxAge(state)) return false;
        if (worldIn.isRemote) return true;

        worldIn.setBlockState(pos, getDefaultState());
        List<ItemStack> drops = addAdditionalDrops(worldIn, pos,
                EnchantmentHelper.getMaxEnchantmentLevel(Enchantments.FORTUNE, playerIn), Lists.newArrayList(),
                worldIn.rand);

        for (ItemStack drop : drops) spawnAsEntity(worldIn, pos, drop);
        return true;
    }

    protected List<ItemStack> addAdditionalDrops(IBlockAccess world, BlockPos pos, int fortune, List<ItemStack> drops,
                                                 Random random) {
        drops.add(new ItemStack(ModItems.ENTROPY_SHARD.get(), 1));
        if (random.nextInt(20) <= (1 + fortune)) drops.add(new ItemStack(ModItems.ENTROPY_SHARD.get(), 1));
        return drops;
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
                                   IPlantable plantable) {
        if (state.getBlock() == ModBlocks.ENTROPY_CROP_BLOCK.get()) return true;
        return super.canSustainPlant(state, world, pos, direction, plantable);
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return true;
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return ESSENCE_AABB;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return ESSENCE_AABB;
    }

    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        return true;
    }

    public int getAge(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return getAge(state);
    }

    public IBlockState withAge(int age) {
        return this.getDefaultState().withProperty(AGE, age);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return withAge(meta);
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        double d0 = pos.getX();
        double d1 = pos.getY();
        double d2 = pos.getZ();
        if (worldIn.getBlockState(pos.up()).getMaterial() == Material.AIR) {
            if (rand.nextInt(50) == 0) {
                double d8 = d0 + (double) rand.nextFloat();
                double d4 = d1 + stateIn.getBoundingBox(worldIn, pos).maxY;
                double d6 = d2 + (double) rand.nextFloat();
                worldIn.spawnParticle(EnumParticleTypes.PORTAL, d8, d4, d6, 0.0D, 0.5D, 0.0D);
                worldIn.playSound(d8, d4, d6, SoundEvents.BLOCK_LAVA_POP, SoundCategory.BLOCKS,
                        0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }

            if (rand.nextInt(100) == 0) {
                worldIn.playSound(d0, d1, d2, SoundEvents.BLOCK_LAVA_AMBIENT, SoundCategory.BLOCKS,
                        0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
            }
        }
    }

    @Override
    public void handleAssets() {
        var item = Item.getItemFromBlock(this);
        EndReforked.getProxy().registerItemRenderer(item, 0, "inventory");
        EndReforked.getProxy().registerItemRenderer(item, 3, "inventory");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        tooltip.add(I18n.format("tile.dragon_essence.tooltip"));
    }
}
