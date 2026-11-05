package io.github.srdjanv.endreforked.api.worldgen.features;

import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import io.github.srdjanv.endreforked.api.worldgen.GenConfig;
import io.github.srdjanv.endreforked.api.worldgen.base.*;

public class BushSurfaceGenerator extends PositionedFeature {

    protected final BlockBush block;

    public BushSurfaceGenerator(GenConfig genConfig, BlockBush block) {
        super(genConfig, Locators.OFFSET_16.andThenLocate(Locators.SURFACE_AIR));
        this.block = block;
    }

    @Override
    protected boolean doGenerate(WorldServer server, Random rand, BlockPos startPos) {
        int count = 0;
        for (int i = 0; i < config.amount() * 2; ++i) {
            if (count > config.amount()) break;
            BlockPos blockpos = startPos.add(
                    rand.nextInt(8) - rand.nextInt(8),
                    rand.nextInt(4) - rand.nextInt(4),
                    rand.nextInt(8) - rand.nextInt(8));

            if (server.isAirBlock(blockpos) && blockpos.getY() < server.getHeight() - 1) {
                var newState = getState(server, rand, blockpos);
                if (block.canBlockStay(server, blockpos, newState)) {
                    server.setBlockState(blockpos, newState, 2);
                    count++;
                }
            }
        }

        return count > config.amount();
    }

    public IBlockState getState(World world, Random random, BlockPos pos) {
        return block.getDefaultState();
    }

    @Override
    protected PositionValidator getStartPosValidator() {
        return PositionValidators.BLOCK_DOWN_ANY;
    }
}
