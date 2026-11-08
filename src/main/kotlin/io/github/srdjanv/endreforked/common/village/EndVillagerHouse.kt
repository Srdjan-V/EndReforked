package io.github.srdjanv.endreforked.common.village

import net.minecraft.block.BlockDoor
import net.minecraft.block.BlockStairs
import net.minecraft.block.BlockTorch
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.minecraft.world.gen.structure.StructureBoundingBox
import net.minecraft.world.gen.structure.StructureComponent
import net.minecraft.world.gen.structure.StructureVillagePieces.PieceWeight
import net.minecraft.world.gen.structure.StructureVillagePieces.Start
import net.minecraft.world.gen.structure.StructureVillagePieces.Village
import net.minecraftforge.fml.common.registry.VillagerRegistry.IVillageCreationHandler
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession
import java.util.Random

class EndVillagerHouse : Village {
    // Structure pieces are loaded from NBT through the public no-arg constructor
    constructor() : super()

    constructor(start: Start, type: Int, box: StructureBoundingBox, facing: EnumFacing) : super(start, type) {
        coordBaseMode = facing
        boundingBox = box
    }

    private var averageGroundLevel = -1

    private fun stairs(block: net.minecraft.block.Block, facing: EnumFacing): IBlockState =
        block.defaultState.withProperty(BlockStairs.FACING, facing)

    override fun addComponentParts(world: World, random: Random, box: StructureBoundingBox): Boolean {
        if (averageGroundLevel < 0) {
            averageGroundLevel = getAverageGroundLevel(world, box)
            if (averageGroundLevel < 0) return true
            boundingBox.offset(0, averageGroundLevel - boundingBox.maxY + 9 - 1, 0)
        }
        fun put(state: IBlockState, x: Int, y: Int, z: Int) = setBlockState(world, state, x, y, z, box)
        fun fill(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int, state: IBlockState) =
            fillWithBlocks(world, box, x1, y1, z1, x2, y2, z2, state, state, false)

        val planks = Blocks.PLANKS.defaultState
        val slab = Blocks.WOODEN_SLAB.defaultState
        val cobble = Blocks.COBBLESTONE.defaultState
        val stoneBrick = Blocks.STONEBRICK.defaultState
        val bars = Blocks.IRON_BARS.defaultState
        val air = Blocks.AIR.defaultState
        fun door(facing: EnumFacing) = Blocks.DARK_OAK_DOOR.defaultState.withProperty(BlockDoor.FACING, facing)
        fun torch(facing: EnumFacing) = Blocks.TORCH.defaultState.withProperty(BlockTorch.FACING, facing)

        put(stairs(Blocks.STONE_STAIRS, EnumFacing.NORTH), 4, 0, 0)

        // Floor
        fill(2, 0, 1, 6, 0, 4, slab)
        fill(1, 0, 1, 1, 0, 4, planks)
        fill(7, 0, 1, 7, 0, 4, planks)

        fill(0, 0, 0, 0, 2, 5, cobble)
        fill(8, 0, 0, 8, 2, 5, cobble)
        fill(1, 0, 0, 7, 1, 0, cobble)
        fill(1, 0, 5, 7, 1, 5, cobble)

        fill(0, 3, 0, 0, 3, 5, planks)
        fill(8, 3, 0, 8, 3, 5, planks)
        fill(1, 2, 0, 7, 3, 0, planks)
        fill(1, 2, 5, 7, 3, 5, planks)

        // Ceiling
        fill(1, 4, 1, 7, 4, 4, slab)
        fill(0, 4, 1, 8, 4, 1, stoneBrick)
        fill(0, 4, 4, 8, 4, 4, stoneBrick)
        fill(0, 5, 2, 8, 5, 3, planks)
        put(planks, 0, 4, 2)
        put(planks, 0, 4, 3)
        put(planks, 8, 4, 2)
        put(planks, 8, 4, 3)

        buildRoof(world, box)

        // Sides of windows
        put(planks, 0, 2, 1)
        put(planks, 0, 2, 4)
        put(planks, 8, 2, 1)
        put(planks, 8, 2, 4)

        // Windows on east side
        put(bars, 0, 2, 2)
        put(bars, 0, 2, 3)
        // Stairs over window
        val eastStairs = stairs(Blocks.OAK_STAIRS, EnumFacing.EAST)
        put(eastStairs, -1, 3, 2)
        put(eastStairs, -1, 3, 3)

        // Windows on west side
        put(bars, 8, 2, 2)
        put(bars, 8, 2, 3)
        // Stairs over window
        val westStairs = stairs(Blocks.OAK_STAIRS, EnumFacing.WEST)
        put(westStairs, 9, 3, 2)
        put(westStairs, 9, 3, 3)

        // Windows garden side
        put(bars, 2, 2, 5)
        put(bars, 3, 2, 5)
        put(bars, 4, 2, 5)
        // Windows front side
        put(bars, 5, 2, 0)
        put(bars, 6, 2, 5)

        put(door(EnumFacing.NORTH), 2, 1, 0)
        put(door(EnumFacing.NORTH).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), 2, 2, 0)
        put(torch(EnumFacing.NORTH), 2, 3, 1)

        if (isAirBlockAtCurrentPosition(world, BlockPos(2, 0, -1), box) && !isAirBlockAtCurrentPosition(world, BlockPos(2, -1, -1), box)) {
            put(stairs(Blocks.OAK_STAIRS, EnumFacing.NORTH), 2, 0, -1)
        }

        put(air, 6, 1, 5)
        put(air, 6, 2, 5)
        put(torch(EnumFacing.SOUTH), 6, 3, 4)
        put(door(EnumFacing.SOUTH), 6, 1, 5)
        put(door(EnumFacing.SOUTH).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), 6, 2, 5)

        for (i in 0 until 5) {
            for (j in 0 until 9) {
                clearCurrentPositionBlocksUpwards(world, j, 7, i, box)
                replaceAirAndLiquidDownwards(world, cobble, j, -1, i, box)
            }
        }
        return true
    }

    private fun buildRoof(world: World, box: StructureBoundingBox) {
        val north = stairs(Blocks.OAK_STAIRS, EnumFacing.NORTH)
        val south = stairs(Blocks.OAK_STAIRS, EnumFacing.SOUTH)
        for (z in -1..2) {
            for (x in 0..8) {
                setBlockState(world, north, x, 4 + z, z, box)
                setBlockState(world, south, x, 4 + z, 5 - z, box)
            }
        }
        spawnVillagers(world, box, 4, 1, 2, 1)
    }

    private fun isAirBlockAtCurrentPosition(world: World, pos: BlockPos, box: StructureBoundingBox): Boolean {
        val state = getBlockStateFromPos(world, pos.x, pos.y, pos.z, box)
        return state.block.isAir(state, world, pos)
    }

    override fun chooseForgeProfession(count: Int, prof: VillagerProfession): VillagerProfession = EndVillagerHandler.profExplorer

    class VillageManager : IVillageCreationHandler {
        override fun buildComponent(
            villagePiece: PieceWeight, startPiece: Start, pieces: List<StructureComponent>, random: Random,
            p1: Int, p2: Int, p3: Int, facing: EnumFacing, p5: Int,
        ): Village? {
            val box = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, 0, 0, 11, 10, 9, facing)
            // Same check as the protected Village.canVillageGoDeeper
            return if (box.minY <= 10 || StructureComponent.findIntersecting(pieces, box) != null) null
            else EndVillagerHouse(startPiece, p5, box, facing)
        }

        override fun getVillagePieceWeight(random: Random, i: Int) =
            PieceWeight(EndVillagerHouse::class.java, 15, MathHelper.getInt(random, i, 1 + i))

        override fun getComponentClass(): Class<*> = EndVillagerHouse::class.java
    }
}
