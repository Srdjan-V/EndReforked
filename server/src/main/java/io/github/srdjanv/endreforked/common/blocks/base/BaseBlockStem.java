package io.github.srdjanv.endreforked.common.blocks.base;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import io.github.srdjanv.endreforked.EndReforked;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class BaseBlockStem extends BlockStem {

    protected final List<Block> sustainableBlocks = new ObjectArrayList<>();

    protected BaseBlockStem(String name, Block crop) {
        super(crop);
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

    protected abstract Item getSeed();
}
