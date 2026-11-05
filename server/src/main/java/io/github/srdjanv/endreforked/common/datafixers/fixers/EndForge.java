package io.github.srdjanv.endreforked.common.datafixers.fixers;

import java.util.Collections;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;

import org.jetbrains.annotations.NotNull;

import io.github.srdjanv.endreforked.common.datafixers.providers.BlockMappingProvider;
import io.github.srdjanv.endreforked.common.datafixers.providers.ItemMappingProvider;

public class EndForge implements BlockMappingProvider, ItemMappingProvider {

    @Override
    public int getFixVersion() {
        return 1;
    }

    @Override
    public FixTypes getType() {
        return FixTypes.ITEM_INSTANCE;
    }

    @Override
    public @NotNull Map<ResourceLocation, ResourceLocation> getCommonMappings() {
        return Collections.singletonMap(resLoc("block_end_forge"), resLoc("end_forge_block"));
    }
}
