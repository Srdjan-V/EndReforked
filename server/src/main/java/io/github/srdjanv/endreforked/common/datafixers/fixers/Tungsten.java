package io.github.srdjanv.endreforked.common.datafixers.fixers;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;

import org.jetbrains.annotations.NotNull;

import io.github.srdjanv.endreforked.common.datafixers.providers.BlockMappingProvider;
import io.github.srdjanv.endreforked.common.datafixers.providers.ItemMappingProvider;

public class Tungsten implements IFixableData, ItemMappingProvider, BlockMappingProvider {

    @Override
    public FixTypes getType() {
        return FixTypes.ITEM_INSTANCE;
    }

    @Override
    public int getFixVersion() {
        return 1;
    }

    @Override
    public @NotNull NBTTagCompound fixTagCompound(NBTTagCompound compound) {
        return compound;
    }

    @Override
    public @NotNull Map<ResourceLocation, ResourceLocation> getCommonMappings() {
        Map<ResourceLocation, ResourceLocation> mappings = new HashMap<>();
        mappings.put(
                resLoc("block_wolframium"),
                resLoc("tungsten_block"));
        mappings.put(
                resLoc("block_wolframium_ore"),
                resLoc("tungsten_ore"));
        return mappings;
    }

    @Override
    public @NotNull Map<ResourceLocation, ResourceLocation> getItemMappings() {
        Map<ResourceLocation, ResourceLocation> mappings = new HashMap<>();
        mappings.put(
                resLoc("item_ingot_wolframium"),
                resLoc("tungsten_ingot"));
        mappings.put(
                resLoc("wolframium_nugget"),
                resLoc("tungsten_nugget"));

        return mappings;
    }
}
