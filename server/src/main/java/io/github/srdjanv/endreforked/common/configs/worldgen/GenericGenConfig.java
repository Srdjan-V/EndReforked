package io.github.srdjanv.endreforked.common.configs.worldgen;

import java.util.Objects;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import io.github.srdjanv.endreforked.api.worldgen.GenConfig;
import io.github.srdjanv.endreforked.api.worldgen.Generator;
import io.github.srdjanv.endreforked.api.worldgen.WorldGenHandler;
import io.github.srdjanv.endreforked.api.worldgen.features.BushSurfaceGenerator;
import io.github.srdjanv.endreforked.api.worldgen.features.FilledSphereGenerator;
import io.github.srdjanv.endreforked.api.worldgen.features.RadiusSurfaceGenerator;
import io.github.srdjanv.endreforked.api.worldgen.features.SphereGenerator;
import io.github.srdjanv.endreforked.common.ModBioms;
import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.blocks.BlockDragoniteCrop;
import io.github.srdjanv.endreforked.common.configs.base.ResourceLocationWrapper;
import io.github.srdjanv.endreforked.common.configs.worldgen.base.WorldGenBaseConfigReloadable;

public class GenericGenConfig extends WorldGenBaseConfigReloadable {

    private static GenericGenConfig instance;

    public static GenericGenConfig getInstance() {
        if (Objects.isNull(instance)) instance = new GenericGenConfig();
        return instance;
    }

    private GenericGenConfig() {
        super("generic_gen");
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        registerGen("lormyte",
                builder -> {
                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(80)
                                    .setRadius(12)
                                    .setMaxHeight(45)
                                    .setMinHeight(20).build(),
                            1);

                    return builder.build();
                },
                (world, biome, config) -> {
                    return new SphereGenerator(config, (server, configL, rand, pos) -> {
                        var block = server.getBlockState(pos).getBlock();
                        return block == Blocks.END_STONE || block == ModBlocks.END_MOSS_BLOCK.get();
                    }, (server, rand, pos) -> {
                        if (rand.nextInt(100) > 80) return false;
                        server.setBlockState(pos, ModBlocks.LORMYTE_CRYSTAL_BLOCK.get().getDefaultState());
                        return true;
                    });
                });

        registerGen("fuzzy_end_magma",
                builder -> {
                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(95)
                                    .setRadius(12)
                                    .setMaxHeight(25)
                                    .setMinHeight(10).build(),
                            1);
                    return builder.build();
                },
                (world, biome, config) -> {
                    return new SphereGenerator(config, (server, configL, rand, pos) -> {
                        var block = server.getBlockState(pos).getBlock();
                        return block == Blocks.END_STONE || block == ModBlocks.END_MOSS_BLOCK.get();
                    }, (server, rand, pos) -> {
                        if (rand.nextInt(100) > 80) return false;
                        if (rand.nextInt(100) > 50) {
                            server.setBlockState(pos, ModBlocks.END_MAGMA_BLOCK.get().getDefaultState());
                        } else
                            server.setBlockState(pos, ModBlocks.FLUID_END_MAGMA_BLOCK.get().getDefaultState());
                        return true;
                    });
                });

        registerGen("filled_end_magma",
                builder -> {
                    builder.whiteListBiome(
                            GenConfig.builder()
                                    .setRarity(95)
                                    .setRadius(12)
                                    .setSphereFillRatio(6)
                                    .setMaxHeight(25)
                                    .setMinHeight(10).build(),
                            ResourceLocationWrapper.of(ModBioms.ORGANA_BIOME.get().getRegistryName()));
                    return builder.build();
                },
                (world, biome, config) -> {
                    return new FilledSphereGenerator(config, (server, configL, rand, pos) -> {
                        var block = server.getBlockState(pos).getBlock();
                        return block == Blocks.END_STONE || block == ModBlocks.END_MOSS_BLOCK.get();
                    }, (server, rand, pos) -> {
                        server.setBlockState(pos, ModBlocks.FLUID_END_MAGMA_BLOCK.get().getDefaultState());
                        return true;
                    }, (server, rand, pos) -> {
                        if (rand.nextInt(100) > 80) return false;
                        server.setBlockState(pos, ModBlocks.END_MAGMA_BLOCK.get().getDefaultState());
                        return true;
                    });
                });

        registerGen("entropy_end_stone",
                builder -> {
                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(25)
                                    .setAmount(30)
                                    .setMaxHeight(30)
                                    .setMinHeight(10).build(),
                            1);
                    return builder.build();
                },
                (world, biome, config) -> new WorldGenMinable(
                        ModBlocks.ENTROPY_END_STONE.get().getDefaultState(), config.amount(),
                        BlockMatcher.forBlock(Blocks.END_STONE)));

        registerGen("end_moss_patch",
                builder -> {
                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(25)
                                    .setRadius(4)
                                    .setMaxHeight(90)
                                    .setMinHeight(50).build(),
                            1);

                    return builder.build();
                },
                (world, biome, config) -> new RadiusSurfaceGenerator(config,
                        (server, configL, rand, pos) -> server.getBlockState(pos).getBlock() == Blocks.END_STONE,
                        (server, rand, pos) -> {
                            if (rand.nextInt(100) > 80) return false;
                            if (server.isAirBlock(pos) && !server.isAirBlock(pos.up())) return false;
                            server.setBlockState(pos, ModBlocks.END_MOSS_GRASS_BLOCK.get().getDefaultState());
                            if (rand.nextInt(100) > 30) return true;
                            server.setBlockState(pos.up(), ModBlocks.ORGANA_WEED_BLOCK.get().getDefaultState());
                            if (rand.nextInt(100) > 5) return true;
                            var endMoss = ModBlocks.END_MOSS_GRASS_BLOCK.get();
                            endMoss.grow(world, rand, pos, endMoss.getDefaultState());
                            return true;
                        }));

        registerGen("end_coral",
                builder -> {
                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(0)
                                    .setAmount(20)
                                    .setMaxHeight(90)
                                    .setMinHeight(50).build(),
                            1);
                    return builder.build();
                },
                (world, biome, config) -> new BushSurfaceGenerator(config, ModBlocks.END_CORAL.get()));

        registerGen("dragonite_crop",
                builder -> {
                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(5)
                                    .setAmount(6)
                                    .setMaxHeight(90)
                                    .setMinHeight(50).build(),
                            1);
                    return builder.build();
                },
                (world, biome, config) -> new BushSurfaceGenerator(config, ModBlocks.END_CORAL.get()));

        registerGen("end_flower",
                builder -> {
                    builder.whiteListDim(
                            GenConfig.builder()
                                    .setRarity(5)
                                    .setAmount(6)
                                    .setMaxHeight(90)
                                    .setMinHeight(50).build(),
                            1);
                    return builder.build();
                },
                (world, biome, config) -> new BushSurfaceGenerator(config, ModBlocks.DRAGONITE_CROP.get()) {

                    @Override
                    public IBlockState getState(World world, Random random, BlockPos pos) {
                        return block.getDefaultState().withProperty(BlockDragoniteCrop.AGE,
                                random.nextInt(BlockDragoniteCrop.AGE.getAllowedValues().size() - 1));
                    }
                });
    }

    @Override
    public void registerToHandler() {
        var instance = WorldGenHandler.getInstance();
        loadedDataData.forEach((name, worldGenConfiguration) -> {
            Generator generator = worldGenConfiguration.parseConfig(name, nameToGenerator.get(name));
            instance.registerGenericGenerator(generator);
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
