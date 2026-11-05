package io.github.srdjanv.endreforked.common.blocks.base;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class BaseBlockBush extends BlockBush implements InventoryBlockModel {

    protected final List<Block> sustainableBlocks = new ObjectArrayList<>();

    public BaseBlockBush(String name, Material material) {
        super(material);
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
}
