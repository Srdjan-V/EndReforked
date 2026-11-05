package io.github.srdjanv.endreforked.common.bioms;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeEnd;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.BiomeDictionary;

import org.jetbrains.annotations.NotNull;

import git.jbredwards.nether_api.api.biome.IEndBiome;
import git.jbredwards.nether_api.api.biome.INoSpawnBiome;
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator;
import io.github.srdjanv.endreforked.common.ModBlocks;
import io.github.srdjanv.endreforked.common.bioms.base.BiomeDictionaryHandler;

public class EntropyBiome extends BiomeEnd implements IEndBiome, INoSpawnBiome, BiomeDictionaryHandler {

    public EntropyBiome() {
        super(new BiomeProperties("Entropy").setRainDisabled());
        setRegistryName("entropy");

        topBlock = Blocks.END_STONE.getDefaultState();
        fillerBlock = ModBlocks.ENTROPY_END_STONE.get().getDefaultState();
        flowers.clear();
        decorator = createBiomeDecorator();
    }

    @Override
    public @NotNull BiomeDecorator createBiomeDecorator() {
        return new Decorator();
    }

    public void buildSurface(
                             @Nonnull final INetherAPIChunkGenerator chunkGenerator,
                             final int chunkX, final int chunkZ,
                             @Nonnull final ChunkPrimer primer,
                             final int x, final int z,
                             final double terrainNoise) {
        boolean needTop = true;
        int depth = 0;
        for (int y = chunkGenerator.getWorld().getActualHeight() - 1; y >= 0; --y) {
            final IBlockState here = primer.getBlockState(x, y, z);
            if (here.getBlock() == Blocks.END_STONE) {
                if (needTop) {
                    var top = primer.getBlockState(x, y + 1, z);
                    if (top.getMaterial() == Material.AIR) {
                        primer.setBlockState(x, y, z, topBlock);
                        needTop = false;
                    }
                } else if (depth > 5) {
                    if (chunkGenerator.getRand().nextInt(100) < 80)
                        primer.setBlockState(x, y, z, fillerBlock);
                } else if (depth > 4) {
                    if (chunkGenerator.getRand().nextInt(100) < 60)
                        primer.setBlockState(x, y, z, fillerBlock);
                } else if (depth > 3) {
                    if (chunkGenerator.getRand().nextInt(100) < 40)
                        primer.setBlockState(x, y, z, fillerBlock);
                } else if (depth > 2) {
                    if (chunkGenerator.getRand().nextInt(100) < 20)
                        primer.setBlockState(x, y, z, fillerBlock);
                } else if (depth > 1) {
                    if (chunkGenerator.getRand().nextInt(100) < 10)
                        primer.setBlockState(x, y, z, fillerBlock);
                }
                depth++;
            }
        }
    }

    @Override
    public void registerToBiomeDictionary() {
        BiomeDictionary.addTypes(
                this,
                BiomeDictionary.Type.END,
                BiomeDictionary.Type.MAGICAL,
                BiomeDictionary.Type.RARE,
                BiomeDictionary.Type.getType("ENTROPY"));
    }

    public static class Decorator extends BiomeDecorator {

        public Decorator() {}

        @Override
        protected void genDecorations(Biome biomeIn, World world, Random rand) {
            super.generateOres(world, rand);
        }
    }
}
