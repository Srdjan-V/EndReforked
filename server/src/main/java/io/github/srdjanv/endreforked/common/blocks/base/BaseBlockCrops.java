package io.github.srdjanv.endreforked.common.blocks.base;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

public class BaseBlockCrops extends BlockCrops implements InventoryBlockModel {

    protected final ObjectList<Block> sustainableBlocks = new ObjectArrayList<>();

    public BaseBlockCrops(String name) {
        setTranslationKey(name);
        setRegistryName(name);
        setCreativeTab(EndReforked.ENDERTAB);
    }

    public List<Block> getSustainableBlocks() {
        return sustainableBlocks;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        return sustainableBlocks.contains(state.getBlock());
    }

    protected boolean handleRightClick(World worldIn, BlockPos pos, IBlockState state, Supplier<ItemStack> drop) {
        if (worldIn.isRemote) return false;
        if (isMaxAge(state)) {
            spawnAsEntity(worldIn, pos, drop.get());
            worldIn.setBlockState(pos, withAge(0));
            return true;
        }
        return false;
    }
}
