package io.github.srdjanv.endreforked.client;

import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ModelLoader;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Maps;

import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlowerStem;

public class StateMappers {

    public static void initOrganaFlowerStem(BlockOrganaFlowerStem organaFlowerStem) {
        ModelLoader.setCustomStateMapper(organaFlowerStem, new StateMapperBase() {

            @Override
            protected @NotNull ModelResourceLocation getModelResourceLocation(IBlockState state) {
                Map<IProperty<?>, Comparable<?>> map = Maps.newLinkedHashMap(state.getProperties());

                if (state.getValue(BlockOrganaFlowerStem.FACING) != EnumFacing.UP) {
                    map.remove(BlockOrganaFlowerStem.AGE);
                }

                return new ModelResourceLocation(Block.REGISTRY.getNameForObject(state.getBlock()),
                        this.getPropertyString(map));
            }
        });
    }
}
