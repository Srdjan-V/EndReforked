package io.github.srdjanv.endreforked.common.blocks;

import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.github.srdjanv.endreforked.common.blocks.base.BlockBase;
import io.github.srdjanv.endreforked.common.entity.EntityChronologist;
import io.github.srdjanv.endreforked.common.entity.EntityWatcher;
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel;

public class BlockEndMagma extends BlockBase implements InventoryBlockModel {

    public BlockEndMagma() {
        super("end_magma_block", Material.ROCK);
        setTickRandomly(true);
        setSoundType(SoundType.STONE);
        setHardness(3.0F);
        setHarvestLevel("pickaxe", 2);
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.PURPLE;
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if (!(entityIn instanceof EntityEnderman) &&
                !(entityIn instanceof EntityWatcher) &&
                !(entityIn instanceof EntityChronologist)) {
            if (!entityIn.isImmuneToFire() && entityIn instanceof EntityLivingBase livingBase) {
                if (!EnchantmentHelper.hasFrostWalkerEnchantment(livingBase))
                    entityIn.attackEntityFrom(DamageSource.HOT_FLOOR, 1.0F);
            }
        }

        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos) {
        return 15728880;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        BlockPos blockpos = pos.up();
        IBlockState iblockstate = worldIn.getBlockState(blockpos);

        if (iblockstate.getBlock() == Blocks.WATER || iblockstate.getBlock() == Blocks.FLOWING_WATER) {
            worldIn.setBlockToAir(blockpos);
            worldIn.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
                    2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

            if (worldIn instanceof WorldServer server) server.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
                    (double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.25D, (double) blockpos.getZ() + 0.5D,
                    8, 0.5D, 0.25D, 0.5D, 0.0D);
        }
    }

    @Override
    public boolean canEntitySpawn(IBlockState state, Entity entityIn) {
        return entityIn.isImmuneToFire();
    }
}
