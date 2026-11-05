package io.github.srdjanv.endreforked.common.entropy.chunks;

import java.util.OptionalInt;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider;
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.api.entropy.world.EntropyChunkReader;
import io.github.srdjanv.endreforked.api.util.Ticker;

public class PassiveEntropyChunkInducer extends PassiveEntropyChunk {

    private final EntropyDataProvider.PassiveInducer passiveInducer;

    public PassiveEntropyChunkInducer(
                                      EntropyChunkReader reader,
                                      Ticker ticker,
                                      int entropy) {
        this(reader, ticker, entropy, () -> EntropyRadius.ONE);
    }

    public PassiveEntropyChunkInducer(
                                      EntropyChunkReader reader,
                                      Ticker ticker,
                                      int baseEntropy,
                                      Supplier<EntropyRadius> radiusModifier) {
        this(reader, ticker, () -> baseEntropy * radiusModifier.get().getChunks());
    }

    public PassiveEntropyChunkInducer(
                                      EntropyChunkReader reader,
                                      Ticker ticker,
                                      IntSupplier entropySup) {
        super(reader, ticker, entropySup);
        passiveInducer = new EntropyDataProvider.PassiveInducer() {

            @Override
            public OptionalInt getFrequency() {
                return OptionalInt.of(ticker.getFrequency());
            }

            @Override
            public int getInduced() {
                return entropy.getAsInt();
            }
        };
    }

    public void induce() {
        if (ticker.tick()) {
            wrapper.getEntropyView().induceEntropy(entropy.getAsInt(), false);
        }
    }

    public EntropyDataProvider.PassiveInducer getPassiveInducer() {
        return passiveInducer;
    }
}
