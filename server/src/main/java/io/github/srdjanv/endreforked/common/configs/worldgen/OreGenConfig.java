package io.github.srdjanv.endreforked.common.configs.worldgen;

import java.util.Objects;

import net.minecraft.block.BlockStone;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import io.github.srdjanv.endreforked.api.worldgen.GenConfig;
import io.github.srdjanv.endreforked.api.worldgen.Generator;
import io.github.srdjanv.endreforked.api.worldgen.WorldGenHandler;
import io.github.srdjanv.endreforked.common.ModBioms;
import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.configs.base.ResourceLocationWrapper;
import io.github.srdjanv.endreforked.common.configs.worldgen.base.WorldGenBaseConfigReloadable;

public class OreGenConfig extends WorldGenBaseConfigReloadable {

    private static OreGenConfig instance;

    public static OreGenConfig getInstance() {
        if (Objects.isNull(instance)) instance = new OreGenConfig();
        return instance;
    }

    private OreGenConfig() {
        super("ore_gen_cfg");
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        registerGen("obsidian_essence_ore",
                builder -> {
                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(80)
                                    .setAmount(20)
                                    .setMaxHeight(256)
                                    .setMinHeight(0).build(),
                            0);

                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(60)
                                    .setAmount(40)
                                    .setMaxHeight(256)
                                    .setMinHeight(0).build(),
                            1);
                    return builder.build();
                },
                (world, biome, config) -> new WorldGenMinable(ModBlocks.OBSIDIAN_ESSENCE_ORE.get().getDefaultState(),
                        config.amount(), BlockMatcher.forBlock(Blocks.OBSIDIAN)));

        registerGen("entropy_ore_crop",
                builder -> {
                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(25)
                                    .setAmount(20)
                                    .setMaxHeight(60)
                                    .setMinHeight(0).build(),
                            1);

                    builder.whiteListBiome(
                            GenConfig.builder()
                                    .setRarity(15)
                                    .setAmount(25)
                                    .setMaxHeight(60)
                                    .setMinHeight(0).build(),
                            ResourceLocationWrapper.of(ModBioms.ORGANA_BIOME.get().getRegistryName()));
                    return builder.build();
                },
                (world, biome, config) -> new WorldGenMinable(ModBlocks.ENTROPY_CROP_BLOCK.get().getDefaultState(),
                        config.amount(), BlockMatcher.forBlock(Blocks.END_STONE)));

        registerGen("tungsten_ore",
                builder -> {
                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(15)
                                    .setAmount(10)
                                    .setMaxHeight(48)
                                    .setMinHeight(0).build(),
                            0);
                    return builder.build();
                },
                (world, biome, config) -> new WorldGenMinable(ModBlocks.TUNGSTEN_ORE.get().getDefaultState(),
                        config.amount(), input -> {
                            if (input != null && input.getBlock() == Blocks.STONE) {
                                return input.getValue(BlockStone.VARIANT).equals(BlockStone.EnumType.DIORITE);
                            } else return false;
                        }));

        registerGen("tungsten_end",
                builder -> {
                    builder.dimConfigFallback(
                            GenConfig.builder()
                                    .setRarity(5)
                                    .setAmount(25)
                                    .setMaxHeight(48)
                                    .setMinHeight(0).build());

                    builder.whiteListDim(1)
                            .whiteListBiome(ResourceLocationWrapper.of("sky"));

                    return builder.build();
                },
                (world, biome, config) -> new WorldGenMinable(
                        ModBlocks.TUNGSTEN_END_ORE.get().getDefaultState(),
                        config.amount(), BlockMatcher.forBlock(Blocks.END_STONE)));
    }

    @Override
    public void registerToHandler() {
        var instance = WorldGenHandler.getInstance();
        loadedDataData.forEach((name, worldGenConfiguration) -> {
            Generator generator = worldGenConfiguration.parseConfig(name, nameToGenerator.get(name));
            instance.registerOreGenerator(generator);
        });
    }

    @Override
    public void unRegisterFromHandler() {
        var instance = WorldGenHandler.getInstance();
        defaultData.keySet().forEach(name -> {
            instance.unregisterOreGenerator(genConfig -> genConfig.getName().equals(name));
        });
    }
}
