package io.github.srdjanv.endreforked.common.bioms

import git.jbredwards.nether_api.api.biome.IEndBiome
import git.jbredwards.nether_api.api.biome.INoSpawnBiome
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.bioms.base.BiomeDictionaryHandler
import io.github.srdjanv.endreforked.common.blocks.BlockOrgana
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlower
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.BiomeDecorator
import net.minecraft.world.biome.BiomeEnd
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraftforge.common.BiomeDictionary
import java.util.Random

class OrganaBiome : BiomeEnd(BiomeProperties("Organa").setRainDisabled()), IEndBiome, INoSpawnBiome, BiomeDictionaryHandler {

    init {
        setRegistryName("organa")
        topBlock = ModBlocks.ORGANA_BLOCK.defaultState
        fillerBlock = ModBlocks.ORGANA_END_STONE_BLOCK.defaultState
        flowers.clear()
        decorator = createBiomeDecorator()
    }

    override fun createBiomeDecorator(): BiomeDecorator = Decorator()

    override fun buildSurface(
        chunkGenerator: INetherAPIChunkGenerator, chunkX: Int, chunkZ: Int, primer: ChunkPrimer,
        x: Int, z: Int, terrainNoise: Double,
    ) {
        var needTop = true
        for (y in chunkGenerator.world.actualHeight - 1 downTo 0) {
            if (primer.getBlockState(x, y, z).block !== Blocks.END_STONE) continue
            if (needTop && primer.getBlockState(x, y + 1, z).material === Material.AIR) {
                primer.setBlockState(x, y, z, topBlock)
                needTop = false
                continue
            }
            primer.setBlockState(x, y, z, fillerBlock)
        }
    }

    override fun generateChorusPlants(chunkGenerator: INetherAPIChunkGenerator, chunkX: Int, chunkZ: Int, islandHeight: Float) = false

    override fun generateIslands(chunkGenerator: INetherAPIChunkGenerator, chunkX: Int, chunkZ: Int, islandHeight: Float) = false

    override fun registerToBiomeDictionary() {
        BiomeDictionary.addTypes(
            this, BiomeDictionary.Type.DENSE, BiomeDictionary.Type.LUSH, BiomeDictionary.Type.END,
            BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.RARE, BiomeDictionary.Type.getType("ORGANA"),
        )
    }

    private class Decorator : BiomeDecorator() {
        override fun genDecorations(biomeIn: Biome, world: World, rand: Random) {
            generateOres(world, rand)
        }
    }
}
