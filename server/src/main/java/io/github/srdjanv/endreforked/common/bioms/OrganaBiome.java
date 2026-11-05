package io.github.srdjanv.endreforked.common.bioms;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
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
import io.github.srdjanv.endreforked.common.blocks.BlockEndMossGrass;
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlower;

public class OrganaBiome extends BiomeEnd implements IEndBiome, INoSpawnBiome, BiomeDictionaryHandler {

    public OrganaBiome() {
        super(new BiomeProperties("Organa").setRainDisabled());
        setRegistryName("organa");

        topBlock = ModBlocks.END_MOSS_GRASS_BLOCK.get().getDefaultState();
        fillerBlock = ModBlocks.END_MOSS_BLOCK.get().getDefaultState();
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
        for (int y = chunkGenerator.getWorld().getActualHeight() - 1; y >= 0; --y) {
            final IBlockState here = primer.getBlockState(x, y, z);
            if (here.getBlock() == Blocks.END_STONE) {
                if (needTop) {
                    var top = primer.getBlockState(x, y + 1, z);
                    if (top.getMaterial() == Material.AIR) {
                        primer.setBlockState(x, y, z, topBlock);
                        needTop = false;
                        continue;
                    }
                }
                primer.setBlockState(x, y, z, fillerBlock);
            }
        }
    }

    @Override
    public boolean generateChorusPlants(@NotNull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ,
                                        float islandHeight) {
        return false;
    }

    @Override
    public boolean generateIslands(@NotNull INetherAPIChunkGenerator chunkGenerator, int chunkX, int chunkZ,
                                   float islandHeight) {
        return false;
    }

    @Override
    public void registerToBiomeDictionary() {
        BiomeDictionary.addTypes(
                this,
                BiomeDictionary.Type.DENSE,
                BiomeDictionary.Type.LUSH,
                BiomeDictionary.Type.END,
                BiomeDictionary.Type.MAGICAL,
                BiomeDictionary.Type.RARE,
                BiomeDictionary.Type.getType("ORGANA"));
    }

    public static class Decorator extends BiomeDecorator {

        public Decorator() {}

        // todo gen mushrooms
        @Override
        protected void genDecorations(Biome biomeIn, World world, Random rand) {
            super.generateOres(world, rand);

            // chunkPos is player pos
            var realChunkPos = new BlockPos(chunkPos.getX() >> 4, 0, chunkPos.getY() >> 4);
            if ((long) realChunkPos.getX() * (long) realChunkPos.getX() +
                    (long) realChunkPos.getZ() * (long) realChunkPos.getZ() > 4096L) {
                final BlockPos pos = chunkPos;
                final int amountInChunk = rand.nextInt(5);
                for (int i = 0; i < amountInChunk; i++) {
                    final int xOffset = rand.nextInt(16) + 8;
                    final int zOffset = rand.nextInt(16) + 8;
                    final int y = world.getHeight(pos.add(xOffset, 0, zOffset)).getY();

                    if (y > 0 && world.isAirBlock(pos.add(xOffset, y, zOffset))) {
                        final IBlockState ground = world.getBlockState(pos.add(xOffset, y - 1, zOffset));
                        if (ground == ModBlocks.END_MOSS_GRASS_BLOCK.get().getDefaultState())
                            BlockOrganaFlower.generatePlant(world, pos.add(xOffset, y, zOffset), rand, 8);
                    }
                }
            }

            final BlockPos pos = chunkPos;
            final int xOffset = rand.nextInt(16) + 8;
            final int zOffset = rand.nextInt(16) + 8;
            final int y = world.getHeight(pos.add(xOffset, 0, zOffset)).getY();

            if (y > 0 && world.isAirBlock(pos.add(xOffset, y, zOffset))) {
                final var currentPos = pos.add(xOffset, y - 1, zOffset);
                final var currentBlockState = world.getBlockState(currentPos);
                final var ground = currentBlockState.getBlock();
                if (ground instanceof BlockEndMossGrass grass) {
                    if (grass.canGrow(world, currentPos, currentBlockState, false))
                        grass.grow(world, rand, currentPos, currentBlockState);
                }
            }
        }
    }
}
