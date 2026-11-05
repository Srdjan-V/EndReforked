package io.github.srdjanv.endreforked.common.datafixers;

import java.util.Map;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.google.common.collect.Maps;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.datafixers.fixers.*;
import io.github.srdjanv.endreforked.common.datafixers.providers.BlockMappingProvider;
import io.github.srdjanv.endreforked.common.datafixers.providers.CommonMappingProvider;
import io.github.srdjanv.endreforked.common.datafixers.providers.ItemMappingProvider;
import io.github.srdjanv.endreforked.utils.Initializer;

public final class Fixer implements Initializer {

    private static Fixer instance;

    public static Fixer getInstance() {
        if (Objects.isNull(instance)) instance = new Fixer();
        return instance;
    }

    private final ModFixs modFixer;

    public ModFixs getModFixer() {
        return modFixer;
    }

    private Fixer() {
        modFixer = FMLCommonHandler.instance().getDataFixer()
                .init(Tags.MODID, Tags.DATAFIXER_VERSION);

        registerFix(new Tungsten());
        registerFix(new Xorcite());
        registerFix(new Materializer());
        registerFix(new EndForge());
        registerFix(new GenericBlocks());
        registerFix(new GenericItems());
    }

    private void registerFix(CommonMappingProvider mappingProvider) {
        modFixer.registerFix(mappingProvider.getType(), mappingProvider);
    }

    @Override
    public void registerEventBus() {
        registerThisToEventBus();
    }

    private Map<ResourceLocation, ResourceLocation> getBlockMappings() {
        Map<ResourceLocation, ResourceLocation> mappingProviders = Maps.newHashMap();
        for (IFixableData fix : modFixer.getFixes(FixTypes.ITEM_INSTANCE)) {
            if (fix instanceof CommonMappingProvider provider)
                mappingProviders.putAll(provider.getCommonMappings());
            if (fix instanceof BlockMappingProvider provider)
                mappingProviders.putAll(provider.getBlockMappings());
        }
        return mappingProviders;
    }

    @SubscribeEvent
    public void missingBlockMapping(RegistryEvent.MissingMappings<Block> event) {
        var mappings = getBlockMappings();
        for (RegistryEvent.MissingMappings.Mapping<Block> entry : event.getAllMappings()) {
            ResourceLocation oldName = entry.key;
            ResourceLocation newName = mappings.get(oldName);
            if (newName != null) {
                var newBlock = ForgeRegistries.BLOCKS.getValue(newName);
                if (newBlock != null) {
                    entry.remap(newBlock);
                } else {
                    EndReforked.LOGGER.error("Unable to locate block: {}", newName);
                    EndReforked.LOGGER.error("Report it at: {}", Tags.ISSUES_URL);
                    entry.warn();
                }
            }
        }
    }

    private Map<ResourceLocation, ResourceLocation> getItemMappings() {
        Map<ResourceLocation, ResourceLocation> mappingProviders = Maps.newHashMap();
        for (IFixableData fix : modFixer.getFixes(FixTypes.ITEM_INSTANCE)) {
            if (fix instanceof CommonMappingProvider provider)
                mappingProviders.putAll(provider.getCommonMappings());
            if (fix instanceof ItemMappingProvider provider)
                mappingProviders.putAll(provider.getItemMappings());
        }
        return mappingProviders;
    }

    @SubscribeEvent
    public void missingItemMapping(RegistryEvent.MissingMappings<Item> event) {
        var mappings = getItemMappings();
        for (RegistryEvent.MissingMappings.Mapping<Item> entry : event.getAllMappings()) {
            ResourceLocation oldName = entry.key;
            ResourceLocation newName = mappings.get(oldName);
            if (newName != null) {
                Item newItem = ForgeRegistries.ITEMS.getValue(newName);
                if (newItem != null) {
                    entry.remap(newItem);
                } else {
                    EndReforked.LOGGER.error("Unable to locate item: {}", newName);
                    EndReforked.LOGGER.error("Report it at: {}", Tags.ISSUES_URL);
                    entry.warn();
                }
            }
        }
    }
}
