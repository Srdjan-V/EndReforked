package io.github.srdjanv.endreforked.common.blocks.base;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel;

public class BaseBlockCrops extends BlockCrops implements InventoryBlockModel {

    public BaseBlockCrops(String name) {
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReforked.endertab);
    }

    protected boolean handleRightClick(World worldIn, BlockPos pos, IBlockState state, ItemStack drop) {
        if (worldIn.isRemote) return false;
        if (isMaxAge(state)) {
            spawnAsEntity(worldIn, pos, drop);
            worldIn.setBlockState(pos, withAge(0));
            return true;
        }
        return false;
    }
}
