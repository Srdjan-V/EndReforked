package io.github.srdjanv.endreforked.common.fluids.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.base.crafting.HandlerRegistry;
import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import io.github.srdjanv.endreforked.api.fluids.base.CollisionRecipe;
import io.github.srdjanv.endreforked.api.fluids.base.FluidEntityCollisionHandler;
import io.github.srdjanv.endreforked.api.fluids.entropy.EntropyFluidAnyStateCollisionHandler;
import io.github.srdjanv.endreforked.api.fluids.entropy.EntropyFluidCollisionHandler;
import io.github.srdjanv.endreforked.api.fluids.entropy.EntropyFluidEntityCollisionHandler;
import io.github.srdjanv.endreforked.common.ModFluids;
import io.github.srdjanv.endreforked.common.fluids.base.BaseBlockFluid;
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.FluidEntropyTile;

public class BlockFluidEntropy extends BaseBlockFluid implements IFluidInteractable {

    public BlockFluidEntropy() {
        super("entropy", ModFluids.ENTROPY.get(), Material.WATER, MapColor.PURPLE);
        setTickRandomly(true);
        setQuantaPerBlock(6);
        setTickRate(10);
        setHardness(1000.0F);
        setLightOpacity(1);
        enableStats = false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return state.getValue(LEVEL) == 0;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return state.getValue(LEVEL) == 0 ? new FluidEntropyTile() : null;
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entityIn) {
        onEntityCollisionIFluidInteractable(world, pos, state, entityIn);
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        randomTickIFluidInteractable(world, pos, state, random);
    }

    @Override
    public @Nullable HandlerRegistry<IBlockState, CollisionRecipe<IBlockState, IBlockState>> getFluidCollisionRegistry() {
        return EntropyFluidCollisionHandler.INSTANCE;
    }

    @Override
    public @Nullable HandlerRegistry<Block, CollisionRecipe<Block, IBlockState>> getAnyFluidCollisionRegistry() {
        return EntropyFluidAnyStateCollisionHandler.INSTANCE;
    }

    @Override
    public @Nullable FluidEntityCollisionHandler getEntityFluidCollisionRegistry() {
        return EntropyFluidEntityCollisionHandler.INSTANCE;
    }
}
