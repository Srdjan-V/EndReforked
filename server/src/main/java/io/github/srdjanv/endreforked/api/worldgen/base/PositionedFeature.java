package io.github.srdjanv.endreforked.api.worldgen.base;

import java.util.*;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.feature.WorldGenerator;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.EndReforked;
import io.github.srdjanv.endreforked.api.worldgen.GenConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class PositionedFeature extends WorldGenerator implements SpacedGen {

    protected final GenConfig config;
    protected final PositionValidator validator;
    protected final List<Locator> locators;
    protected boolean isSpacedGenDisabled = false;

    protected PositionedFeature(GenConfig config, Locator... locators) {
        this(config, new ObjectArrayList<>(locators));
    }

    protected PositionedFeature(GenConfig config, PositionValidator validator, Locator... locators) {
        this(config, validator, new ObjectArrayList<>(locators));
    }

    protected PositionedFeature(GenConfig config, List<Locator> locator) {
        this(config, null, new ObjectArrayList<>(locator));
    }

    protected PositionedFeature(GenConfig config, @Nullable PositionValidator validator, List<Locator> locator) {
        this.config = config;
        this.validator = validator == null ? PositionValidators.ALWAYS_TRUE : validator;
        this.locators = locator;
    }

    @Override
    public void setSpacedGenState(boolean disabled) {
        isSpacedGenDisabled = disabled;
    }

    @Override
    public boolean isSpacedGenDisabled() {
        return isSpacedGenDisabled;
    }

    @Nullable
    protected BlockPos getStartPos(WorldServer server, Random rand, BlockPos pos, PositionValidator validator) {
        BlockPos retPos = null;
        for (Locator locator : locators) {
            retPos = locator.compute(server, rand, config, pos, validator);
            if (Objects.nonNull(retPos)) break;
        }
        return retPos;
    }

    @Override
    public final boolean generate(World worldIn, Random rand, BlockPos position) {
        if (!(worldIn instanceof WorldServer server)) {
            EndReforked.LOGGER.error("Unable to run world generator on ClientWorld");
            return false;
        }

        BlockPos newPos = getStartPos(server, rand, position, getStartPosValidator());
        if (Objects.isNull(newPos)) return false;
        if (shouldGenSpaced(config) && !validateGenSpacing(server, config, position))
            return false;
        return doGenerate(server, rand, newPos);
    }

    protected abstract boolean doGenerate(WorldServer server, Random rand, BlockPos startPos);

    protected PositionValidator getStartPosValidator() {
        return validator;
    }
}
