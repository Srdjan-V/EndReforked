package io.github.srdjanv.endreforked.common.worldgen

import com.google.common.base.Predicate
import io.github.srdjanv.endreforked.api.worldgen.Feature
import io.github.srdjanv.endreforked.api.worldgen.FeatureKind
import io.github.srdjanv.endreforked.api.worldgen.GenParams
import io.github.srdjanv.endreforked.api.worldgen.Step
import io.github.srdjanv.endreforked.api.worldgen.WorldFeature
import io.github.srdjanv.endreforked.common.blocks.BlockOrganaFlower
import io.github.srdjanv.endreforked.common.registry.Registries
import io.github.srdjanv.endreforked.impl.worldgen.WorldFeatures
import io.github.srdjanv.endreforked.impl.worldgen.isStructureChunk
import io.github.srdjanv.endreforked.impl.worldgen.paramsAt
import net.minecraft.block.BlockBush
import net.minecraft.block.IGrowable
import net.minecraft.block.properties.PropertyInteger
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.IChunkGenerator
import net.minecraft.world.gen.feature.WorldGenMinable
import net.minecraft.world.gen.structure.StructureBoundingBox
import net.minecraft.world.gen.structure.template.PlacementSettings
import net.minecraftforge.common.BiomeDictionary
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.IWorldGenerator
import net.minecraftforge.fml.common.registry.GameRegistry
import org.apache.logging.log4j.Logger
import java.util.Random

/** Registers the world generator and the End biomes. Call it once in the init phase, after the registries are built. */
object WorldGen {
    @JvmStatic
    fun install(registries: Registries, logger: Logger) {
        GameRegistry.registerWorldGenerator(FeatureGenerator({ registries.worldFeatures.current }, logger), 0)
        val biomes = EndBiomeSetup(registries.endBiomes, logger)
        MinecraftForge.EVENT_BUS.register(biomes)
        biomes.apply()
        biomes.watchChanges()
    }
}

/**
 * Runs the world features of the registry for every new chunk. It reads the current snapshot for each chunk, so a rebuild is used by the next chunk.
 * The registry decides where and how often a feature runs ([paramsAt]), this class only places the blocks.
 */
class FeatureGenerator(private val registry: () -> WorldFeatures, private val logger: Logger) : IWorldGenerator {
    override fun generate(random: Random, chunkX: Int, chunkZ: Int, world: World, chunkGenerator: IChunkGenerator, chunkProvider: IChunkProvider) {
        val server = world as? WorldServer ?: return
        val features = registry()
        // The biome in the middle of the chunk decides for the whole chunk.
        val biome = world.getBiome(BlockPos((chunkX shl 4) + 8, world.actualHeight, (chunkZ shl 4) + 8))
        val biomeId = biome.registryName ?: return
        val types = BiomeDictionary.getTypes(biome)
        val dimension = world.provider.dimension
        val origin = BlockPos(chunkX shl 4, world.seaLevel, chunkZ shl 4)
        for (kind in FeatureKind.entries) for (feature in features.of(kind)) {
            val params = feature.paramsAt(dimension, biomeId, types) ?: continue
            if (params.rarity >= 1 && random.nextInt(params.rarity) != 0) continue
            try {
                place(server, random, feature, params, origin)
            } catch (e: Exception) {
                throw IllegalStateException("World feature ${feature.id} failed in chunk $chunkX, $chunkZ", e)
            }
        }
    }

    private fun place(world: WorldServer, rand: Random, f: WorldFeature, p: GenParams, origin: BlockPos) {
        when (val ft = f.feature) {
            is Feature.Ore -> ore(world, rand, ft, p, origin)
            is Feature.Sphere -> sphere(world, rand, ft, p, origin)
            is Feature.FilledSphere -> filledSphere(world, rand, ft, p, origin)
            is Feature.RadiusSurface -> radiusSurface(world, rand, ft, p, origin)
            is Feature.Bush -> bush(world, rand, ft, p, origin)
            is Feature.Template -> template(world, rand, f, ft, p, origin)
        }
    }

    private fun ore(world: WorldServer, rand: Random, ft: Feature.Ore, p: GenParams, origin: BlockPos) {
        val y = p.minHeight + rand.nextInt(maxOf(1, p.maxHeight - p.minHeight))
        val replace = ft.replace
        WorldGenMinable(ft.block, p.amount, Predicate<IBlockState> { it == replace }).generate(world, rand, BlockPos(origin.x + 8, y, origin.z + 8))
    }

    private fun sphere(world: WorldServer, rand: Random, ft: Feature.Sphere, p: GenParams, origin: BlockPos) {
        val center = locate(world, rand, p, origin, RANDOM_HEIGHT) ?: return
        forEachInSphere(center, p.radius) { pos ->
            if (world.getBlockState(pos) in ft.replace && rand.nextInt(100) < ft.chance) {
                val alt = ft.alt
                world.setBlockState(pos, if (alt != null && rand.nextInt(100) < ft.altChance) alt else ft.block)
            }
        }
    }

    private fun filledSphere(world: WorldServer, rand: Random, ft: Feature.FilledSphere, p: GenParams, origin: BlockPos) {
        val center = locate(world, rand, p, origin, RANDOM_HEIGHT) ?: return
        forEachInSphere(center, p.radius) { pos ->
            if (world.getBlockState(pos) in ft.replace) {
                if (center.getDistance(pos.x, pos.y, pos.z) <= p.fillRadius) world.setBlockState(pos, ft.inner)
                else if (rand.nextInt(100) < ft.outerChance) world.setBlockState(pos, ft.outer)
            }
        }
    }

    private fun radiusSurface(world: WorldServer, rand: Random, ft: Feature.RadiusSurface, p: GenParams, origin: BlockPos) {
        val start = locate(world, rand, p, origin, SURFACE) ?: return
        val r = p.radius
        for (x in start.x - r..start.x + r) for (z in start.z - r..start.z + r) {
            val dx = x - start.x
            val dz = z - start.z
            if (dx * dx + dz * dz > r * r) continue
            // The surface is at the height of the start or one block below it.
            var pos = BlockPos(x, start.y, z)
            if (world.isAirBlock(pos)) {
                pos = pos.down()
                if (world.isAirBlock(pos)) continue
            }
            if (!world.isAirBlock(pos.up()) || world.getBlockState(pos) != ft.replace) continue
            if (rand.nextInt(100) >= ft.chance) continue
            world.setBlockState(pos, ft.ground)
            if (rand.nextInt(100) >= ft.plantChance) continue
            world.setBlockState(pos.up(), ft.plant)
            if (rand.nextInt(100) >= ft.growChance) continue
            (ft.ground.block as? IGrowable)?.grow(world, rand, pos, ft.ground)
        }
    }

    private fun bush(world: WorldServer, rand: Random, ft: Feature.Bush, p: GenParams, origin: BlockPos) {
        val block = ft.block.block as BlockBush
        var y = world.actualHeight
        val x = origin.x + 16
        val z = origin.z + 16
        // The air block on top of the first solid block from the top.
        while (y >= p.minHeight && !(world.isAirBlock(BlockPos(x, y, z)) && !world.isAirBlock(BlockPos(x, y - 1, z)))) y--
        if (y < p.minHeight) return
        val start = BlockPos(x, y, z)
        var count = 0
        for (i in 0 until p.amount * 2) {
            if (count > p.amount) break
            val pos = start.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8))
            if (world.isAirBlock(pos) && pos.y < world.height - 1) {
                val state = if (ft.randomAge) withRandomAge(ft.block, rand) else ft.block
                if (block.canBlockStay(world, pos, state)) {
                    world.setBlockState(pos, state, 2)
                    count++
                }
            }
        }
    }

    /** A crop starts with an age below its maximum. */
    private fun withRandomAge(state: IBlockState, rand: Random): IBlockState {
        val age = state.block.blockState.getProperty("age") as? PropertyInteger ?: return state
        return state.withProperty(age, age.allowedValues.toList()[rand.nextInt(age.allowedValues.size - 1)])
    }

    private fun template(world: WorldServer, rand: Random, f: WorldFeature, ft: Feature.Template, p: GenParams, origin: BlockPos) {
        val start = locate(world, rand, p, origin, ft.locator) ?: return
        val spacing = p.spacing
        if (spacing != null && !spacing.isStructureChunk(start.x shr 4, start.z shr 4) { x, z, salt -> world.setRandomSeed(x, z, salt) }) return
        val template = world.structureTemplateManager.get(world.minecraftServer, ft.structure)
        if (template == null) {
            logger.error("Unable to find the structure {} of {}", ft.structure, f.id)
            return
        }
        // The structure stays in the chunk of its start. One block in from the corner, so that it triggers no update in the next chunk.
        val chunk = ChunkPos(start)
        val settings = PlacementSettings().setBoundingBox(StructureBoundingBox(chunk.xStart, 0, chunk.zStart, chunk.xEnd, 255, chunk.zEnd))
        template.addBlocksToWorld(world, chunk.getBlock(1, start.y, 1), settings)
        for (flower in ft.flowers) {
            if (flower.chance < 100 && rand.nextInt(100) >= flower.chance) continue
            val at = locate(world, rand, p, origin, flower.locator) ?: continue
            BlockOrganaFlower.generatePlant(world, at.up(), rand, 8 + rand.nextInt(3))
        }
    }

    /** Applies the steps, starting at [from]. Null when a step finds nothing. */
    private fun locate(world: WorldServer, rand: Random, p: GenParams, from: BlockPos, steps: List<Step>): BlockPos? {
        var pos = from
        for (step in steps) {
            pos = when (step) {
                is Step.Move -> pos.add(step.dx, step.dy, step.dz)
                Step.RandomHeight -> BlockPos(pos.x, p.minHeight + rand.nextInt(maxOf(1, p.maxHeight - p.minHeight)), pos.z)
                is Step.Surface -> surface(world, p, pos, step) ?: return null
            }
        }
        return pos
    }

    private fun surface(world: WorldServer, p: GenParams, at: BlockPos, step: Step.Surface): BlockPos? {
        for (y in world.actualHeight downTo p.minHeight) {
            val pos = BlockPos(at.x, y, at.z)
            if (!world.isAirBlock(pos) && (step.on == null || world.getBlockState(pos) == step.on)) return pos
        }
        return null
    }

    private fun forEachInSphere(center: BlockPos, radius: Int, action: (BlockPos) -> Unit) {
        for (x in -radius..radius) for (y in -radius..radius) for (z in -radius..radius) {
            if (x * x + y * y + z * z <= radius * radius) action(center.add(x, y, z))
        }
    }

    private companion object {
        /** The spheres start one chunk to the east and south, at a random height. */
        val RANDOM_HEIGHT = listOf(Step.Move(16, 0, 16), Step.RandomHeight)

        /** The surface of the chunk to the east and south. */
        val SURFACE = listOf(Step.Move(16, 0, 16), Step.Surface())
    }
}
