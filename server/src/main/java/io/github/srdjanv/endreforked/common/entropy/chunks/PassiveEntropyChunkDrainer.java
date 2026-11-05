package io.github.srdjanv.endreforked.common.entropy.chunks;

import java.util.OptionalInt;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider;
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.api.entropy.world.EntropyChunkReader;
import io.github.srdjanv.endreforked.api.util.Ticker;

public class PassiveEntropyChunkDrainer extends PassiveEntropyChunk {

    private final EntropyDataProvider.PassiveDrainer passiveDrainer;

    public PassiveEntropyChunkDrainer(
                                      EntropyChunkReader reader,
                                      Ticker ticker,
                                      int entropy) {
        this(reader, ticker, entropy, () -> EntropyRadius.ONE);
    }

    public PassiveEntropyChunkDrainer(
                                      EntropyChunkReader reader,
                                      Ticker ticker,
                                      int baseEntropy,
                                      Supplier<EntropyRadius> radiusModifier) {
        this(reader, ticker, () -> baseEntropy * radiusModifier.get().getRadius());
    }

    public PassiveEntropyChunkDrainer(
                                      EntropyChunkReader reader,
                                      Ticker ticker,
                                      IntSupplier entropySup) {
        super(reader, ticker, entropySup);
        passiveDrainer = new EntropyDataProvider.PassiveDrainer() {

            @Override
            public OptionalInt getFrequency() {
                return OptionalInt.of(ticker.getFrequency());
            }

            @Override
            public int getDrained() {
                return entropy.getAsInt();
            }
        };
    }

    public void drain() {
        if (ticker.tick()) {
            wrapper.getEntropyView().drainEntropy(entropy.getAsInt(), false);
        }
    }

    public EntropyDataProvider.PassiveDrainer getPassiveDrainer() {
        return passiveDrainer;
    }
}
