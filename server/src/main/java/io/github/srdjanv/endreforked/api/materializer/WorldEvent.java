package io.github.srdjanv.endreforked.api.materializer;

import static net.minecraft.util.EnumParticleTypes.CRIT;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import org.jetbrains.annotations.NotNull;

import io.github.srdjanv.endreforked.api.util.Structure;

public final class WorldEvent implements Comparable<WorldEvent> {

    private final int chance;
    private final Structure structure;
    private final BlockAction blockAction;

    public static WorldEvent create(int chance, Structure structure, BlockAction blockAction) {
        return new WorldEvent(chance, structure, blockAction);
    }

    private WorldEvent(int chance, Structure structure, BlockAction blockAction) {
        this.chance = chance;
        this.structure = structure;
        this.blockAction = blockAction;
    }

    public int getChance() {
        return chance;
    }

    public Structure getStructure() {
        return structure;
    }

    public BlockAction getBlockAction() {
        return blockAction;
    }

    @Override
    public int compareTo(@NotNull WorldEvent o) {
        return chance - o.chance;
    }

    public static BlockAction replaceEachPosWithBlockState(IBlockState block) {
        return (worldServer, blockWorldState) -> {
            if (worldServer.isAirBlock(blockWorldState.getPos())) return;
            worldServer.setBlockState(blockWorldState.getPos(), block);
        };
    }

    public static BlockAction replaceEachPosWithDefaultBlockState(Block block) {
        return (worldServer, blockWorldState) -> {
            if (worldServer.isAirBlock(blockWorldState.getPos())) return;
            worldServer.setBlockState(blockWorldState.getPos(), block.getDefaultState());
        };
    }

    public static BlockAction replaceEachPosWithBlockFromItemStack(ItemStack item) {
        return replaceEachPosWithBlockFromItem(item.getItem());
    }

    public static BlockAction replaceEachPosWithBlockFromItem(Item item) {
        var blockFormItem = Block.getBlockFromItem(item);
        return replaceEachPosWithDefaultBlockState(blockFormItem);
    }

    public static BlockAction replaceEachPosWithItemStack(ItemStack item) {
        return (worldServer, blockWorldState) -> {
            if (worldServer.isAirBlock(blockWorldState.getPos())) return;
            worldServer.setBlockToAir(blockWorldState.getPos());
            Block.spawnAsEntity(worldServer, blockWorldState.getPos(), item);
        };
    }

    public static BlockAction replaceEachPosWithItem(Item item) {
        return replaceEachPosWithItemStack(new ItemStack(item));
    }

    @FunctionalInterface
    public interface BlockAction {

        void run(WorldServer worldServer, BlockWorldState blockWorldState);

        default void particles(WorldServer worldServer, BlockWorldState blockWorldState) {
            transformBlockParticles(worldServer, CRIT, blockWorldState.getBlockState(), blockWorldState.getPos());
        }

        // TODO: 26/10/2023 test
        default void transformBlockParticles(WorldServer world, EnumParticleTypes type, IBlockState state,
                                             BlockPos pos) {
            AxisAlignedBB bounds = state.getBoundingBox(world, pos);

            for (int i = 0; i <= 6; i++) {
                double startX = pos.getX() + bounds.minX;
                double endX = pos.getX() + bounds.maxX;

                double startY = pos.getY() + bounds.minY;
                double endY = pos.getY() + bounds.maxY;

                double startZ = pos.getZ() + bounds.minZ;
                double endZ = pos.getZ() + bounds.maxZ;

                iterateBlockFace(world, type, startX, endX, startY, endY, startZ, endZ);
            }
        }

        default void iterateBlockFace(WorldServer world, EnumParticleTypes type, double fromX, double toX,
                                      double fromY, double toY, double fromZ, double toZ) {
            for (double x = fromX; x < toX; x += 0.3)
                for (double y = fromY; y < toY; y += 0.3)
                    for (double z = fromZ; z < toZ; z += 0.3)
                        world.spawnParticle(type, x, y, z,
                                1, 0, 0, 0, 0.2);
        }
    }
}
