package io.github.srdjanv.endreforked.common.configs.worldgen;

import java.util.Objects;
import java.util.Random;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.api.worldgen.GenConfig;
import io.github.srdjanv.endreforked.api.worldgen.Generator;
import io.github.srdjanv.endreforked.api.worldgen.WorldGenHandler;
import io.github.srdjanv.endreforked.api.worldgen.base.*;
import io.github.srdjanv.endreforked.api.worldgen.features.TemplateGenerator;
import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlower;
import io.github.srdjanv.endreforked.common.configs.worldgen.base.WorldGenBaseConfigReloadable;

public class StructureGenConfig extends WorldGenBaseConfigReloadable {

    private static StructureGenConfig instance;

    public static StructureGenConfig getInstance() {
        if (Objects.isNull(instance)) instance = new StructureGenConfig();
        return instance;
    }

    public StructureGenConfig() {
        super("structure_gen_cfg");
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        registerGen("end_ruins",
                builder -> {
                    builder.whiteListBiome("sky");
                    builder.whiteListBiomeType(BiomeDictionary.Type.END);
                    builder.dimConfigFallback(
                            GenConfig.builder()
                                    .setUniqueGeneratorId("end_ruins".hashCode())
                                    .setSpacing(16)
                                    .setSeparation(10)
                                    .setMaxHeight(100)
                                    .setMinHeight(85).build());
                    return builder.build();
                },
                (world, biome, config) -> new TemplateGenerator(config, "end_ruins"));
        registerGen("end_endermite_island",
                builder -> {
                    builder.whiteListBiome("sky", "plains", "desert", "ocean", "deep_ocean", "savanna");
                    builder.dimConfigFallback(
                            GenConfig.builder()
                                    .setUniqueGeneratorId("end_endermite_island".hashCode())
                                    .setSpacing(64)
                                    .setSeparation(50)
                                    .setMaxHeight(115)
                                    .setMinHeight(90)
                                    .build());

                    return builder.build();
                },
                (world, biome, config) -> new TemplateGenerator(config, "end_endermite_island"));
        registerGen("end_entropy_island",
                builder -> {
                    builder.whiteListBiome("sky", "plains", "desert", "ocean", "deep_ocean", "savanna");
                    builder.dimConfigFallback(GenConfig.builder()
                            .setRarity(600)
                            .setMaxHeight(115)
                            .setMinHeight(90)
                            .build());
                    return builder.build();
                },
                (world, biome, config) -> new TemplateGenerator(config, "end_entropy_island"));
        registerGen("end_moss_island",
                builder -> {
                    builder.whiteListBiomeType(
                            GenConfig.builder()
                                    .setRarity(120)
                                    .setMaxHeight(115)
                                    .setMinHeight(90)
                                    .build(),
                            "ORGANA");

                    builder.whiteListBiome("sky", "plains", "desert", "ocean", "deep_ocean", "savanna");
                    builder.dimConfigFallback(
                            GenConfig.builder()
                                    .setUniqueGeneratorId("end_endermite_island".hashCode())
                                    .setSpacing(64)
                                    .setSeparation(50)
                                    .setMaxHeight(115)
                                    .setMinHeight(90)
                                    .build());

                    return builder.build();
                },
                (world, biome, config) -> {
                    class OrganaFlowerFeature extends PositionedFeature {

                        OrganaFlowerFeature(GenConfig config, Locator... locators) {
                            super(config, locators);
                        }

                        OrganaFlowerFeature(GenConfig config, int offset) {
                            super(config, Locators.offsetOf(offset).andThenLocate(Locators.SURFACE_BLOCK));
                        }

                        @Override
                        protected boolean doGenerate(WorldServer server, Random rand, BlockPos startPos) {
                            BlockOrganaFlower.generatePlant(server, startPos.up(), rand, 8 + rand.nextInt(3));
                            return true;
                        }

                        @Override
                        protected PositionValidator getStartPosValidator() {
                            return (server, config, rand, pos) -> server.getBlockState(pos).getBlock() ==
                                    ModBlocks.END_MOSS_GRASS_BLOCK.get();
                        }
                    }
                    return new GeneratorWrapper(new TemplateGenerator(config, "end_moss_island"))
                            .subGen((server, rand, position) -> new OrganaFlowerFeature(config, 7))
                            .subGen((server, rand, position) -> {
                                if (rand.nextInt(100) > 30) return null;
                                Locator loc = (serverL, randL, configL, pos, validator) -> pos.add(4, 0, 9);
                                loc = loc.andThenLocate(Locators.SURFACE_BLOCK);
                                return new OrganaFlowerFeature(config, loc);
                            })
                            .subGen((server, rand, position) -> {
                                if (rand.nextInt(100) > 30) return null;
                                Locator loc = (serverL, randL, configL, pos, validator) -> pos.add(11, 0, 6);
                                loc = loc.andThenLocate(Locators.SURFACE_BLOCK);
                                return new OrganaFlowerFeature(config, loc);
                            });
                });
        registerGen("observatory",
                builder -> {
                    builder.whiteListBiome("desert", "ocean", "deep_ocean", "forest", "birch_forest", "swampland");
                    builder.dimConfigFallback(GenConfig.builder()
                            .setUniqueGeneratorId("observatory".hashCode())
                            .setSpacing(64)
                            .setSeparation(50)
                            .setMaxHeight(3)
                            .setMinHeight(3)
                            .build());

                    return builder.build();
                },
                (world, biome, config) -> new TemplateGenerator(config, "observ"));
        registerGen("ship_wreck",
                builder -> {
                    builder.whiteListBiome("sky");
                    builder.whiteListBiomeType(BiomeDictionary.Type.END);

                    builder.dimConfigFallback(
                            GenConfig.builder()
                                    .setUniqueGeneratorId("ship_wreck".hashCode())
                                    .setSpacing(64)
                                    .setSeparation(50)
                                    .setMaxHeight(70)
                                    .setMinHeight(50).build());

                    return builder.build();
                },
                (world, biome, config) -> new TemplateGenerator(
                        Locators.OFFSET_10.andThenLocate(Locators.SURFACE_BLOCK).andThenMove(pos -> pos.add(0, -2, 2)),
                        config, new ResourceLocation(Tags.MODID, "end_shipwreck"), TemplateGenerator.DEFAULT_SETTINGS));
    }

    @Override
    public void registerToHandler() {
        var instance = WorldGenHandler.getInstance();
        loadedDataData.forEach((name, worldGenConfiguration) -> {
            Generator generator = worldGenConfiguration.parseConfig(name, nameToGenerator.get(name));
            instance.registerStructureGenerator(generator);
        });
    }

    @Override
    public void unRegisterFromHandler() {
        var instance = WorldGenHandler.getInstance();
        defaultData.keySet().forEach(name -> {
            instance.unregisterStructureGenerator(genConfig -> genConfig.getName().equals(name));
        });
    }
}
