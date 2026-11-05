package io.github.srdjanv.endreforked.common.datafixers.providers;

import java.util.Collections;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;

import org.jetbrains.annotations.NotNull;

public interface ItemMappingProvider extends CommonMappingProvider {

    @Override
    default FixTypes getType() {
        return FixTypes.ITEM_INSTANCE;
    }

    @NotNull
    default Map<ResourceLocation, ResourceLocation> getItemMappings() {
        return Collections.emptyMap();
    }
}
