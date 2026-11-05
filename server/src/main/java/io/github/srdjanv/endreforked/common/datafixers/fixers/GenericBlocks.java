package io.github.srdjanv.endreforked.common.datafixers.fixers;

import java.util.Map;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import io.github.srdjanv.endreforked.common.datafixers.providers.BlockMappingProvider;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class GenericBlocks implements BlockMappingProvider {

    @Override
    public int getFixVersion() {
        return 1;
    }

    @Override
    public @NotNull Map<ResourceLocation, ResourceLocation> getCommonMappings() {
        Map<ResourceLocation, ResourceLocation> mapping = new Object2ObjectOpenHashMap<>();
        mapping.put(resLoc("block_end_stone_smooth"), resLoc("end_stone_smooth_block"));
        mapping.put(resLoc("smooth_end_stone_wall"), resLoc("end_stone_smooth_wall"));
        mapping.put(resLoc("smooth_end_stone_stairs"), resLoc("end_stone_smooth_stairs"));
        mapping.put(resLoc("block_end_stone_pillar"), resLoc("end_stone_pillar"));
        mapping.put(resLoc("chiseled_end_bricks"), resLoc("end_bricks_chiseled"));
        mapping.put(resLoc("e_end_bricks_stairs"), resLoc("end_bricks_stairs"));
        mapping.put(resLoc("e_end_bricks_wall"), resLoc("end_bricks_wall"));

        mapping.put(resLoc("block_entropy_end_stone"), resLoc("entropy_end_stone"));

        mapping.put(resLoc("block_endorium"), resLoc("endorium_block"));
        mapping.put(resLoc("block_purpur_lamp"), resLoc("purpur_lamp_block"));
        mapping.put(resLoc("e_purpur_wall"), resLoc("purpur_wall"));

        mapping.put(resLoc("crop_ender_flower"), resLoc("ender_flower_crop"));
        mapping.put(resLoc("broken_ender_flower"), resLoc("ender_flower_broken"));

        mapping.put(resLoc("crop_dragonite"), resLoc("dragonite_crop"));
        mapping.put(resLoc("block_essence_ore"), resLoc("essence_ore"));
        mapping.put(resLoc("block_phantom"), resLoc("phantom_block"));
        mapping.put(resLoc("block_lormyte_crystal"), resLoc("lormyte_crystal_block"));
        mapping.put(resLoc("block_decorative_lormyte"), resLoc("decorative_lormyte_block"));
        mapping.put(resLoc("block_end_magma"), resLoc("end_magma_block"));

        mapping.put(resLoc("block_rune"), resLoc("rune_block"));

        return mapping;
    }
}
