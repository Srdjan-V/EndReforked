package io.github.srdjanv.endreforked.common.bioms

import git.jbredwards.nether_api.api.biome.IEndBiome
import git.jbredwards.nether_api.api.biome.INoSpawnBiome
import git.jbredwards.nether_api.api.world.INetherAPIChunkGenerator
import io.github.srdjanv.endreforked.common.ModBlocks
import io.github.srdjanv.endreforked.common.bioms.base.BiomeDictionaryHandler
import io.github.srdjanv.endreforked.common.blocks.BlockEndMossGrass
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlower
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.Biome.BiomeProperties
import net.minecraft.world.biome.BiomeDecorator
import net.minecraft.world.biome.BiomeEnd
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraftforge.common.BiomeDictionary
import java.util.Random

class OrganaBiome : BiomeEnd(BiomeProperties("Organa").setRainDisabled()), IEndBiome, INoSpawnBiome, BiomeDictionaryHandler {

    init {
        setRegistryName("organa")
        topBlock = ModBlocks.END_MOSS_GRASS_BLOCK.get().defaultState
        fillerBlock = ModBlocks.END_MOSS_BLOCK.get().defaultState
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

    // todo gen mushrooms
    private class Decorator : BiomeDecorator() {
        override fun genDecorations(biomeIn: Biome, world: World, rand: Random) {
            generateOres(world, rand)
            val pos = chunkPos

            // chunkPos is player pos
            val cx = (pos.x shr 4).toLong()
            val cz = (pos.z shr 4).toLong()
            if (cx * cx + cz * cz > 4096L) {
                repeat(rand.nextInt(5)) {
                    val xOffset = rand.nextInt(16) + 8
                    val zOffset = rand.nextInt(16) + 8
                    val y = world.getHeight(pos.add(xOffset, 0, zOffset)).y

                    if (y > 0 && world.isAirBlock(pos.add(xOffset, y, zOffset))) {
                        val ground = world.getBlockState(pos.add(xOffset, y - 1, zOffset))
                        if (ground == ModBlocks.END_MOSS_GRASS_BLOCK.get().defaultState)
                            BlockOrganaFlower.generatePlant(world, pos.add(xOffset, y, zOffset), rand, 8)
                    }
                }
            }

            val xOffset = rand.nextInt(16) + 8
            val zOffset = rand.nextInt(16) + 8
            val y = world.getHeight(pos.add(xOffset, 0, zOffset)).y

            if (y > 0 && world.isAirBlock(pos.add(xOffset, y, zOffset))) {
                val currentPos = pos.add(xOffset, y - 1, zOffset)
                val currentBlockState = world.getBlockState(currentPos)
                val ground = currentBlockState.block
                if (ground is BlockEndMossGrass && ground.canGrow(world, currentPos, currentBlockState, false))
                    ground.grow(world, rand, currentPos, currentBlockState)
            }
        }
    }
}
