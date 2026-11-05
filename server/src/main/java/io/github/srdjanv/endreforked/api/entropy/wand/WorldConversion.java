package io.github.srdjanv.endreforked.api.entropy.wand;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import org.jetbrains.annotations.Nullable;

import io.github.srdjanv.endreforked.common.configs.Configs;

public class WorldConversion {

    private final Predicate<IBlockState> matcher;
    private final Supplier<IBlockState> newState;
    private final BiConsumer<WorldServer, BlockPos> conversionCallback;
    private final int itemDamage;

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {

        private Builder() {}

        private static final BiConsumer<WorldServer, BlockPos> sound = (worldServer, pos) -> worldServer.playSound(null,
                pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F,
                worldServer.rand.nextFloat() * 0.4F + 0.8F);

        private static final Predicate<IBlockState> trueMatcher = state -> true;

        private Predicate<IBlockState> matcher;
        private Supplier<IBlockState> newState;
        private int itemDamage = Configs.SERVER_SIDE_CONFIGS.entropyWandUseDamage;
        private BiConsumer<WorldServer, BlockPos> conversionCallback;

        public Builder matcherAny() {
            this.matcher = trueMatcher;
            return this;
        }

        public Builder matcher(Block matcher) {
            this.matcher = state -> state.equals(matcher.getDefaultState());
            return this;
        }

        public Builder matcher(IBlockState matcher) {
            this.matcher = state -> state.equals(matcher);
            return this;
        }

        public Builder matcher(Predicate<IBlockState> matcher) {
            this.matcher = matcher;
            return this;
        }

        public Builder newState(Block newState) {
            return newState(newState.getDefaultState());
        }

        public Builder newState(IBlockState newState) {
            this.newState = () -> newState;
            return this;
        }

        public Builder newState(Supplier<IBlockState> newState) {
            this.newState = newState;
            return this;
        }

        public Builder conversionCallback(BiConsumer<WorldServer, BlockPos> conversionCallback) {
            if (this.conversionCallback != null) {
                this.conversionCallback = this.conversionCallback.andThen(conversionCallback);
            } else this.conversionCallback = conversionCallback;
            return this;
        }

        public Builder playFlintSound() {
            if (conversionCallback != null) {
                conversionCallback = conversionCallback.andThen(sound);
            } else conversionCallback = sound;
            return this;
        }

        public Builder itemDamage(int itemDamage) {
            this.itemDamage = itemDamage;
            return this;
        }

        public Builder addItemDamage(int itemDamage) {
            this.itemDamage += itemDamage;
            return this;
        }

        public WorldConversion build() {
            return new WorldConversion(
                    Objects.requireNonNull(matcher),
                    Objects.requireNonNull(newState),
                    itemDamage,
                    conversionCallback);
        }
    }

    public WorldConversion(Predicate<IBlockState> matcher,
                           Supplier<IBlockState> newState,
                           int itemDamage,
                           @Nullable BiConsumer<WorldServer, BlockPos> conversionCallback) {
        this.matcher = matcher;
        this.newState = newState;
        this.itemDamage = itemDamage;
        this.conversionCallback = conversionCallback;
    }

    public Predicate<IBlockState> getBlockStateMatcher() {
        return matcher;
    }

    public Supplier<IBlockState> getNewState() {
        return newState;
    }

    public int getItemDamage() {
        return itemDamage;
    }

    @Nullable
    public BiConsumer<WorldServer, BlockPos> getConversionCallback() {
        return conversionCallback;
    }
}
