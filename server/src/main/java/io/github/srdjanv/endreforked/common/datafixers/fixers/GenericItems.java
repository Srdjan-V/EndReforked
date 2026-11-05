package io.github.srdjanv.endreforked.common.datafixers.fixers;

import java.util.Map;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import io.github.srdjanv.endreforked.common.datafixers.providers.ItemMappingProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class GenericItems implements ItemMappingProvider {

    @Override
    public int getFixVersion() {
        return 1;
    }

    @Override
    public @NotNull Map<ResourceLocation, ResourceLocation> getItemMappings() {
        Map<ResourceLocation, ResourceLocation> mappings = new Object2ObjectOpenHashMap<>();
        mappings.put(resLoc("item_dragonite_seeds"), resLoc("dragonite_berries"));
        mappings.put(resLoc("food_dragonite_berries"), resLoc("dragonite_berries"));
        mappings.put(resLoc("item_dragonite_tea"), resLoc("dragonite_tea"));
        mappings.put(resLoc("food_chorus_soup"), resLoc("chorus_soup"));

        return mappings;
    }
}
