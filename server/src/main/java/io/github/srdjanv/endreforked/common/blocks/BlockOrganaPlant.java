package io.github.srdjanv.endreforked.common.blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.ModItems;
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase;

public class BlockOrganaPlant extends BlockBase {

    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");

    public BlockOrganaPlant() {
        super("organa_plant", Material.PLANTS, MapColor.PURPLE);
        this.setDefaultState(
                blockState.getBaseState()
                        .withProperty(NORTH, Boolean.FALSE)
                        .withProperty(EAST, Boolean.FALSE)
                        .withProperty(SOUTH, Boolean.FALSE)
                        .withProperty(WEST, Boolean.FALSE)
                        .withProperty(UP, Boolean.FALSE)
                        .withProperty(DOWN, Boolean.FALSE));

        setHarvestLevel("pickaxe", 1);
        setHardness(1.25f);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        Block down = worldIn.getBlockState(pos.down()).getBlock();
        Block up = worldIn.getBlockState(pos.up()).getBlock();
        Block north = worldIn.getBlockState(pos.north()).getBlock();
        Block east = worldIn.getBlockState(pos.east()).getBlock();
        Block south = worldIn.getBlockState(pos.south()).getBlock();
        Block west = worldIn.getBlockState(pos.west()).getBlock();
        var organaFlower = ModBlocks.ORGANA_FLOWER_BLOCK.get();
        return state
                .withProperty(DOWN,
                        down == this || down == organaFlower || down == ModBlocks.END_MOSS_GRASS_BLOCK.get())
                .withProperty(UP, up == this || up == organaFlower)
                .withProperty(NORTH, north == this || north == organaFlower)
                .withProperty(EAST, east == this || east == organaFlower)
                .withProperty(SOUTH, south == this || south == organaFlower)
                .withProperty(WEST, west == this || west == organaFlower);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        state = state.getActualState(source, pos);
        float f1 = state.getValue(WEST) ? 0.0F : 0.1875F;
        float f2 = state.getValue(DOWN) ? 0.0F : 0.1875F;
        float f3 = state.getValue(NORTH) ? 0.0F : 0.1875F;
        float f4 = state.getValue(EAST) ? 1.0F : 0.8125F;
        float f5 = state.getValue(UP) ? 1.0F : 0.8125F;
        float f6 = state.getValue(SOUTH) ? 1.0F : 0.8125F;
        return new AxisAlignedBB(f1, f2, f3, f4, f5, f6);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
                                      List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn,
                                      boolean isActualState) {
        if (!isActualState) {
            state = state.getActualState(worldIn, pos);
        }

        float f = 0.1875F;
        float f1 = 0.8125F;
        addCollisionBoxToList(pos, entityBox, collidingBoxes,
                new AxisAlignedBB(0.1875D, 0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D));

        if (state.getValue(WEST)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes,
                    new AxisAlignedBB(0.0D, 0.1875D, 0.1875D, 0.1875D, 0.8125D, 0.8125D));
        }

        if (state.getValue(EAST)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes,
                    new AxisAlignedBB(0.8125D, 0.1875D, 0.1875D, 1.0D, 0.8125D, 0.8125D));
        }

        if (state.getValue(UP)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes,
                    new AxisAlignedBB(0.1875D, 0.8125D, 0.1875D, 0.8125D, 1.0D, 0.8125D));
        }

        if (state.getValue(DOWN)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes,
                    new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.8125D, 0.1875D, 0.8125D));
        }

        if (state.getValue(NORTH)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes,
                    new AxisAlignedBB(0.1875D, 0.1875D, 0.0D, 0.8125D, 0.8125D, 0.1875D));
        }

        if (state.getValue(SOUTH)) {
            addCollisionBoxToList(pos, entityBox, collidingBoxes,
                    new AxisAlignedBB(0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D, 1.0D));
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!this.canSurviveAt(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.ORGANA_FRUIT.get();
    }

    @Override
    public int quantityDropped(Random random) {
        return random.nextInt(2);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) ? this.canSurviveAt(worldIn, pos) : false;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (worldIn.isRemote) return;
        if (!this.canSurviveAt(worldIn, pos)) {
            worldIn.scheduleUpdate(pos, this, 1);
        }
    }

    public boolean canSurviveAt(World wordIn, BlockPos pos) {
        boolean upAir = wordIn.isAirBlock(pos.up());
        boolean downAir = wordIn.isAirBlock(pos.down());

        for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
            BlockPos blockPosFacing = pos.offset(enumfacing);
            Block blockFacing = wordIn.getBlockState(blockPosFacing).getBlock();

            if (blockFacing == this) {
                if (!upAir && !downAir) return false;
                Block blockUnder = wordIn.getBlockState(blockPosFacing.down()).getBlock();
                if (blockUnder == this || blockUnder == ModBlocks.END_MOSS_GRASS_BLOCK.get()) return true;
            }
        }

        Block blockDown = wordIn.getBlockState(pos.down()).getBlock();
        return blockDown == this || blockDown == ModBlocks.END_MOSS_GRASS_BLOCK.get();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
                                        EnumFacing side) {
        Block block = blockAccess.getBlockState(pos.offset(side)).getBlock();
        return block != this && block != Blocks.CHORUS_FLOWER && (side != EnumFacing.DOWN || block != Blocks.END_STONE);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }
    /*
     * public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand
     * hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
     * neighborChanged(state, worldIn, pos, null, null);
     * return true;
     * }
     */
}
