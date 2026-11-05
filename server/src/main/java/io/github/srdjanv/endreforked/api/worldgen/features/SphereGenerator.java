package io.github.srdjanv.endreforked.api.worldgen.features;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import io.github.srdjanv.endreforked.api.worldgen.GenConfig;
import io.github.srdjanv.endreforked.api.worldgen.base.*;

public class SphereGenerator extends PositionedFeature {

    protected final PositionGenerator positionGenerator;
    protected final PositionValidator startPosValidator;
    protected final PositionValidator positionValidator;

    public SphereGenerator(GenConfig genConfig, PositionValidator positionValidator,
                           PositionGenerator positionGenerator) {
        this(genConfig, PositionValidators.ALWAYS_TRUE, positionValidator, positionGenerator);
    }

    public SphereGenerator(GenConfig genConfig, PositionValidator startPosValidator,
                           PositionValidator positionValidator,
                           PositionGenerator positionGenerator) {
        super(genConfig, Locators.OFFSET_16.andThenLocate(Locators.GEN_CONFIG_MIN_MAX));
        this.startPosValidator = startPosValidator;
        this.positionValidator = positionValidator;
        this.positionGenerator = positionGenerator;
    }

    @Override
    protected boolean doGenerate(WorldServer server, Random rand, BlockPos startPos) {
        int radius = config.radius();
        int startX = startPos.getX();
        int startY = startPos.getY();
        int startZ = startPos.getZ();

        for (int x = startX - radius; x <= startX + radius; x++) {
            for (int y = startY - radius; y <= startY + radius; y++) {
                for (int z = startZ - radius; z <= startZ + radius; z++) {
                    if (Math.pow(x - startX, 2) + Math.pow(y - startY, 2) + Math.pow(z - startZ, 2) <=
                            Math.pow(radius, 2)) {
                        var pos = new BlockPos(x, y, z);
                        if (positionValidator.validate(server, config, rand, pos))
                            positionGenerator.generate(server, rand, pos);
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected PositionValidator getStartPosValidator() {
        return startPosValidator;
    }
}
