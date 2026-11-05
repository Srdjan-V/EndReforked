package io.github.srdjanv.endreforked.common.fluids.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.base.crafting.HandlerRegistry;
import io.github.srdjanv.endreforked.api.fluids.IFluidInteractable;
import io.github.srdjanv.endreforked.api.fluids.base.CollisionRecipe;
import io.github.srdjanv.endreforked.api.fluids.base.FluidEntityCollisionHandler;
import io.github.srdjanv.endreforked.api.fluids.organa.OrganaFluidAnyStateCollisionHandler;
import io.github.srdjanv.endreforked.api.fluids.organa.OrganaFluidCollisionHandler;
import io.github.srdjanv.endreforked.api.fluids.organa.OrganaFluidEntityCollisionHandler;
import io.github.srdjanv.endreforked.common.ModFluids;
import io.github.srdjanv.endreforked.common.fluids.base.BaseBlockFluid;
import io.github.srdjanv.endreforked.common.tiles.passiveinducers.FluidEntropyTile;
import io.github.srdjanv.endreforked.utils.WorldUtils;

public class BlockFluidOrgana extends BaseBlockFluid implements IFluidInteractable {

    public BlockFluidOrgana() {
        super("organa", ModFluids.ORGANA.get(), Material.WATER, MapColor.PURPLE);
        setTickRandomly(true);
        setTickRate(10);
        setHardness(800);
        setLightOpacity(2);
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
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        onEntityCollisionIFluidInteractable(worldIn, pos, state, entityIn);
        if (WorldUtils.isClientWorld(worldIn)) return;
        entityIn.fallDistance = 0;
        if (entityIn instanceof EntityPlayer player) {
            if (player.isSneaking()) return;
            player.addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 40, 2));
            player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 120, 2));
            return;
        }
        if (entityIn instanceof EntityLivingBase livingBase) {
            livingBase.addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 40));
            livingBase.addPotionEffect(new PotionEffect(MobEffects.SPEED, 60));
        }
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {
        randomTickIFluidInteractable(world, pos, state, random);
    }

    @Override
    public @Nullable HandlerRegistry<IBlockState, CollisionRecipe<IBlockState, IBlockState>> getFluidCollisionRegistry() {
        return OrganaFluidCollisionHandler.INSTANCE;
    }

    @Override
    public @Nullable HandlerRegistry<Block, CollisionRecipe<Block, IBlockState>> getAnyFluidCollisionRegistry() {
        return OrganaFluidAnyStateCollisionHandler.INSTANCE;
    }

    @Override
    public @Nullable FluidEntityCollisionHandler getEntityFluidCollisionRegistry() {
        return OrganaFluidEntityCollisionHandler.INSTANCE;
    }
}
