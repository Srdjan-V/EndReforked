package io.github.srdjanv.endreforked.api.entropy.wand;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.block.Block;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.EndReforked;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class EntropyWandHandler {

    private static final Map<Block, List<WorldConversion>> converionMap = new Object2ObjectOpenHashMap<>();

    public static Map<Block, List<WorldConversion>> getConverionMap() {
        return converionMap;
    }

    @Nullable
    public static List<WorldConversion> getConversions(Block block) {
        return converionMap.get(block);
    }

    public static void registerConversions(List<Block> blocks, WorldConversion... worldConversions) {
        for (var block : blocks)
            registerConversions(block, worldConversions);
    }

    public static void registerDefaultStateConversion(Block block, Block conversion) {
        registerConversion(block, WorldConversion.builder()
                .matcher(block.getDefaultState())
                .newState(conversion.getDefaultState())
                .playFlintSound().build());
    }

    public static void registerConversion(Block block, WorldConversion worldConversion) {
        registerConversions(block, worldConversion);
    }

    public static void registerConversions(Block block, WorldConversion... worldConversions) {
        List<WorldConversion> oldWorldConversions = converionMap.get(block);
        if (oldWorldConversions != null && !oldWorldConversions.isEmpty()) {
            EndReforked.LOGGER.warn("{}: overwriting conversions for block {}",
                    EntropyWandHandler.class.getSimpleName(),
                    block);
            oldWorldConversions.clear();
        } else {
            oldWorldConversions = new ObjectArrayList<>();
            converionMap.put(block, oldWorldConversions);
        }
        oldWorldConversions.addAll(Arrays.asList(worldConversions));
    }

    public static void addConversions(List<Block> blocks, WorldConversion... worldConversions) {
        for (var block : blocks)
            addConversions(block, worldConversions);
    }

    public static void addConversion(Block block, WorldConversion worldConversion) {
        addConversions(block, worldConversion);
    }

    public static void addConversions(Block block, WorldConversion... worldConversions) {
        var currentConversions = converionMap.computeIfAbsent(block, block1 -> new ObjectArrayList<>());
        currentConversions.addAll(Arrays.asList(worldConversions));
    }

    public static void removeBlockConversion(Block block) {
        converionMap.remove(block);
    }

    public static void removeConversion(Block block, WorldConversion worldConversion) {
        var conversionList = converionMap.get(block);
        if (Objects.isNull(conversionList)) return;
        conversionList.remove(worldConversion);
    }

    public static void removeConversions(Block block, WorldConversion... worldConversions) {
        var conversionList = converionMap.get(block);
        if (Objects.isNull(conversionList)) return;
        conversionList.removeAll(Arrays.asList(worldConversions));
    }

    private EntropyWandHandler() {}
}
