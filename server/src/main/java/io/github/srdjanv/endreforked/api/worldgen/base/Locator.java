package io.github.srdjanv.endreforked.api.worldgen.base;

import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.api.worldgen.GenConfig;

@FunctionalInterface
public interface Locator {

    @Nullable
    BlockPos compute(WorldServer server, Random rand, GenConfig config, BlockPos pos, PositionValidator validator);

    default Locator andThenLocate(final Locator then) {
        Objects.requireNonNull(then);
        return (server, rand, config, pos, validator) -> {
            var computedPos = Locator.this.compute(server, rand, config, pos, validator);
            if (Objects.isNull(computedPos)) return null;
            return then.compute(server, rand, config, computedPos, validator);
        };
    }

    default Locator andThenMove(Function<BlockPos, BlockPos> posFunction) {
        Objects.requireNonNull(posFunction);
        return (server, rand, config, pos, validator) -> {
            var computedPos = Locator.this.compute(server, rand, config, pos, validator);
            if (Objects.isNull(computedPos)) return null;
            return posFunction.apply(computedPos);
        };
    }

    default Locator andThenValidate(PositionValidator validator) {
        Objects.requireNonNull(validator);
        return (server, rand, config, pos, validatorL) -> {
            var computedPos = Locator.this.compute(server, rand, config, pos, validatorL);
            if (Objects.isNull(computedPos)) return null;
            return validator.validate(server, config, rand, computedPos) ? computedPos : null;
        };
    }
}
