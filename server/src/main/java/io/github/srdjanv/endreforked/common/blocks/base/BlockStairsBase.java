package io.github.srdjanv.endreforked.common.blocks.base;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.utils.models.InventoryBlockModel;

public class BlockStairsBase extends BlockStairs implements InventoryBlockModel {

    public BlockStairsBase(String name, Block block) {
        super(block.getDefaultState());
        setTranslationKey(name);
        setHardness(block.getBlockHardness(block.getDefaultState(), null, null));
        setSoundType(block.getSoundType());
        setRegistryName(name);
        setCreativeTab(EndReforked.ENDERTAB);
        useNeighborBrightness = true;
    }
}
