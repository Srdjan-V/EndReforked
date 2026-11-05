package io.github.srdjanv.endreforked.api.fluids;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.IFluidBlock;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.base.crafting.HandlerRegistry;
import io.github.srdjanv.endreforked.api.fluids.base.CollisionRecipe;
import io.github.srdjanv.endreforked.api.fluids.base.FluidEntityCollisionHandler;
import io.github.srdjanv.endreforked.utils.WorldUtils;

public interface IFluidInteractable extends IFluidBlock {

    default PropertyInteger getFluidLevel() {
        return BlockFluidBase.LEVEL;
    }

    @Nullable
    default HandlerRegistry<IBlockState, CollisionRecipe<IBlockState, IBlockState>> getFluidCollisionRegistry() {
        return null;
    }

    @Nullable
    default HandlerRegistry<Block, CollisionRecipe<Block, IBlockState>> getAnyFluidCollisionRegistry() {
        return null;
    }

    @Nullable
    default FluidEntityCollisionHandler getEntityFluidCollisionRegistry() {
        return null;
    }

    void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entityIn);

    default void onEntityCollisionIFluidInteractable(World world, BlockPos pos, IBlockState state, Entity entityIn) {
        if (WorldUtils.isClientWorld(world)) return;
        var server = WorldUtils.castToServerWorld(world);
        interactWithEntity(server, pos, state, entityIn, world.rand);
    }

    default void interactWithEntity(WorldServer world, BlockPos fluidPos, IBlockState fluidState, Entity entityIn,
                                    Random random) {
        var reg = getEntityFluidCollisionRegistry();
        if (reg == null) return;
        var recIter = reg.getFluidRecipe(entityIn);
        while (recIter.hasNext()) {
            var recipe = recIter.next();
            if (recipe.getChanceConsumeSource() > 1 && random.nextInt(recipe.getChance()) != 0) continue;
            if (recipe.isConsumeSource() && fluidState.getValue(getFluidLevel()) != 0) continue;

            var result = recipe.getRecipeFunction().apply(world, entityIn);
            if (result == null) continue;
            if (recipe.isRemoveEntity()) world.removeEntity(entityIn);
            switch (result.getType()) {
                case ENTITY -> {
                    var entity = result.getEntityResult();
                    entity.setPosition(entityIn.posX, entityIn.posY, entityIn.posZ);
                    world.spawnEntity(entity);
                    if (recipe.isConsumeSource() && recipe.getChanceConsumeSource() > 1 &&
                            random.nextInt(recipe.getChanceConsumeSource()) == 0)
                        world.setBlockToAir(fluidPos);
                }
                case STATE -> {
                    var blockState = result.getStateResult();
                    world.setBlockState(fluidPos, blockState);
                }
                default -> throw new IllegalStateException("Unexpected state: " + result);
            }

            recipe.getInteractionCallback().accept(world, fluidPos);
            recipe.getFluidInteractionCallback().accept(world, fluidPos);
            break;
        }
    }

    void randomTick(World world, BlockPos pos, IBlockState state, Random random);

    default void randomTickIFluidInteractable(World world, BlockPos pos, IBlockState state, Random random) {
        if (WorldUtils.isClientWorld(world)) return;
        var server = WorldUtils.castToServerWorld(world);
        // todo maybe convert to tile entity to enable recipe caching per side
        for (EnumFacing value : EnumFacing.VALUES) {
            interactWithBlock(server, pos.offset(value), pos, state, random);
        }
    }

    default void interactWithBlock(WorldServer world, BlockPos pos, BlockPos fluidPos, IBlockState fluidState,
                                   Random random) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock().isAir(state, world, pos) || state.getBlock() == this) return;
        handleFluidCollision(world, pos, state, fluidPos, fluidState, random);
        handleAnyFluidCollision(world, pos, state, fluidPos, fluidState, random);
    }

    default void handleFluidCollision(WorldServer world, BlockPos pos, IBlockState posState, BlockPos fluidPos,
                                      IBlockState fluidState, Random random) {
        var reg = getFluidCollisionRegistry();
        if (reg == null) return;
        var recipe = reg.findRecipe(posState);
        if (recipe == null) return;
        if (recipe.getChanceConsumeSource() > 1 && random.nextInt(recipe.getChance()) != 0) return;
        if (recipe.isConsumeSource() && fluidState.getValue(getFluidLevel()) != 0) return;

        IBlockState result = recipe.getRecipeFunction().apply(posState);
        world.setBlockState(pos, result, 3);
        if (recipe.isConsumeSource() && recipe.getChanceConsumeSource() > 1 &&
                random.nextInt(recipe.getChanceConsumeSource()) == 0)
            world.setBlockToAir(fluidPos);
        recipe.getInteractionCallback().accept(world, pos);
        recipe.getFluidInteractionCallback().accept(world, fluidPos);
    }

    default void handleAnyFluidCollision(WorldServer world, BlockPos pos, IBlockState posState, BlockPos fluidPos,
                                         IBlockState fluidState, Random random) {
        var reg = getAnyFluidCollisionRegistry();
        if (reg == null) return;
        var recipe = reg.findRecipe(posState.getBlock());
        if (recipe == null) return;
        if (recipe.getChanceConsumeSource() > 1 && random.nextInt(recipe.getChance()) != 0) return;
        if (recipe.isConsumeSource() && fluidState.getValue(getFluidLevel()) != 0) return;

        IBlockState result = recipe.getRecipeFunction().apply(posState.getBlock());
        world.setBlockState(pos, result, 3);
        if (recipe.isConsumeSource() && recipe.getChanceConsumeSource() > 1 &&
                random.nextInt(recipe.getChanceConsumeSource()) == 0)
            world.setBlockToAir(fluidPos);
        recipe.getInteractionCallback().accept(world, pos);
        recipe.getFluidInteractionCallback().accept(world, fluidPos);
    }
}
