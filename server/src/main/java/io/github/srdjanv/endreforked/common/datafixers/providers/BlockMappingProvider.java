package io.github.srdjanv.endreforked.common.datafixers.providers;

import java.util.Collections;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;

import org.jetbrains.annotations.NotNull;

public interface BlockMappingProvider extends CommonMappingProvider {

    @Override
    default FixTypes getType() {
        return FixTypes.ITEM_INSTANCE;
    }

    @NotNull
    default Map<ResourceLocation, ResourceLocation> getBlockMappings() {
        return Collections.emptyMap();
    }
}
