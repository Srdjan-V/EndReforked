package io.github.srdjanv.endreforked.api.worldgen.base;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.worldgen.GenConfig;

public enum Locators implements Locator {

    EMPTY {

        @Override
        public @Nullable BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                          PositionValidator validator) {
            if (validator.validate(server, config, rand, pos)) return pos;
            return null;
        }
    },
    VALIDATOR {

        @Override
        public @Nullable BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                          PositionValidator validator) {
            pos = new BlockPos(pos.getX(), server.getActualHeight(), pos.getZ());
            boolean valid = validator.validate(server, config, rand, pos);
            while (!valid) {
                pos = pos.down();
                if (pos.getY() < 0) break;
                valid = validator.validate(server, config, rand, pos);
            }
            return valid ? pos : null;
        }
    },
    SURFACE_BLOCK {

        @Override
        public @Nullable BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                          PositionValidator validator) {
            pos = new BlockPos(pos.getX(), server.getActualHeight(), pos.getZ());
            boolean valid = !server.isAirBlock(pos) && validator.validate(server, config, rand, pos);
            while (!valid) {
                pos = pos.down();
                if (config.minHeight() > pos.getY()) return null;
                valid = !server.isAirBlock(pos) && validator.validate(server, config, rand, pos);
            }
            return pos;
        }
    },
    SURFACE_AIR {

        @Override
        public @Nullable BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                          PositionValidator validator) {
            pos = new BlockPos(pos.getX(), server.getActualHeight(), pos.getZ());
            boolean valid = server.isAirBlock(pos) && validator.validate(server, config, rand, pos);
            while (!valid) {
                pos = pos.down();
                if (config.minHeight() > pos.getY()) return null;
                valid = server.isAirBlock(pos) && validator.validate(server, config, rand, pos);
            }
            return pos;
        }
    },
    OFFSET_16 {

        @Override
        public BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                PositionValidator validator) {
            return new BlockPos(pos.getX() + 16, pos.getY(), pos.getZ() + 16);
        }
    },
    OFFSET_14 {

        @Override
        public BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                PositionValidator validator) {
            return new BlockPos(pos.getX() + 14, pos.getY(), pos.getZ() + 14);
        }
    },
    OFFSET_12 {

        @Override
        public BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                PositionValidator validator) {
            return new BlockPos(pos.getX() + 12, pos.getY(), pos.getZ() + 12);
        }
    },
    OFFSET_10 {

        @Override
        public BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                PositionValidator validator) {
            return new BlockPos(pos.getX() + 10, pos.getY(), pos.getZ() + 10);
        }
    },
    OFFSET_8 {

        @Override
        public BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                PositionValidator validator) {
            return new BlockPos(pos.getX() + 8, pos.getY(), pos.getZ() + 8);
        }
    },
    OFFSET_4 {

        @Override
        public BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                PositionValidator validator) {
            return new BlockPos(pos.getX() + 4, pos.getY(), pos.getZ() + 4);
        }
    },
    OFFSET_2 {

        @Override
        public BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                PositionValidator validator) {
            return new BlockPos(pos.getX() + 2, pos.getY(), pos.getZ() + 2);
        }
    },
    GEN_CONFIG_MIN_MAX {

        @Override
        public BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos,
                                PositionValidator validator) {
            final int heightDiff = config.maxHeight() - config.minHeight();
            return new BlockPos(
                    pos.getX(),
                    config.minHeight() + rand.nextInt(Math.max(1, heightDiff)),
                    pos.getZ());
        }
    };

    public static Locator offsetOf(final int offset) {
        return (server, rand, config, pos, validator) -> new BlockPos(pos.getX() + offset, pos.getY(),
                pos.getZ() + offset);
    }
}
