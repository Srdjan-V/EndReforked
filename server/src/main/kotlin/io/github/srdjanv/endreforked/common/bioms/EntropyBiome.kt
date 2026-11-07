package io.github.srdjanv.endreforked.common.bioms

import git.jbredwards.nether_api.api.biome.IEndBiome
import git.jbredwards.nether_api.api.biome.INoSpawnBiome
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.bioms.base.BiomeDictionaryHandler
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.Biome.BiomeProperties
import net.minecraft.world.biome.BiomeDecorator
import net.minecraft.world.biome.BiomeEnd
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraftforge.common.BiomeDictionary
import java.util.Random

class EntropyBiome : BiomeEnd(BiomeProperties("Entropy").setRainDisabled()), IEndBiome, INoSpawnBiome, BiomeDictionaryHandler {

    init {
        setRegistryName("entropy")
        topBlock = Blocks.END_STONE.defaultState
        fillerBlock = ModBlocks.ENTROPY_END_STONE.defaultState
        flowers.clear()
        decorator = createBiomeDecorator()
    }

    override fun createBiomeDecorator(): BiomeDecorator = Decorator()

    override fun buildSurface(
        chunkGenerator: INetherAPIChunkGenerator, chunkX: Int, chunkZ: Int, primer: ChunkPrimer,
        x: Int, z: Int, terrainNoise: Double,
    ) {
        var needTop = true
        var depth = 0
        for (y in chunkGenerator.world.actualHeight - 1 downTo 0) {
            if (primer.getBlockState(x, y, z).block !== Blocks.END_STONE) continue
            if (needTop) {
                if (primer.getBlockState(x, y + 1, z).material === Material.AIR) {
                    primer.setBlockState(x, y, z, topBlock)
                    needTop = false
                }
            } else {
                val chance = when {
                    depth > 5 -> 80
                    depth > 4 -> 60
                    depth > 3 -> 40
                    depth > 2 -> 20
                    depth > 1 -> 10
                    else -> 0
                }
                if (chance > 0 && chunkGenerator.rand.nextInt(100) < chance) primer.setBlockState(x, y, z, fillerBlock)
            }
            depth++
        }
    }

    override fun registerToBiomeDictionary() {
        BiomeDictionary.addTypes(
            this, BiomeDictionary.Type.END, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.RARE,
            BiomeDictionary.Type.getType("ENTROPY"),
        )
    }

    private class Decorator : BiomeDecorator() {
        override fun genDecorations(biomeIn: Biome, world: World, rand: Random) {
            generateOres(world, rand)
        }
    }
}
