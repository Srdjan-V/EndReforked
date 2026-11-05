package io.github.srdjanv.endreforked.common.datafixers.fixers;

import java.util.Collections;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;

import org.jetbrains.annotations.NotNull;

import io.github.srdjanv.endreforked.common.datafixers.providers.BlockMappingProvider;
import io.github.srdjanv.endreforked.common.datafixers.providers.ItemMappingProvider;

public class Xorcite implements IFixableData, ItemMappingProvider, BlockMappingProvider {

    @Override
    public FixTypes getType() {
        return FixTypes.ITEM_INSTANCE;
    }

    @Override
    public int getFixVersion() {
        return 1;
    }

    @Override
    public @NotNull NBTTagCompound fixTagCompound(@NotNull NBTTagCompound compound) {
        return compound;
    }

    @Override
    public @NotNull Map<ResourceLocation, ResourceLocation> getBlockMappings() {
        return Collections.singletonMap(
                resLoc("dragon_essence"),
                resLoc("xorcite_block"));
    }

    @Override
    public @NotNull Map<ResourceLocation, ResourceLocation> getItemMappings() {
        return Collections.singletonMap(
                resLoc("death_essence"),
                resLoc("xorcite_shard"));
    }
}
