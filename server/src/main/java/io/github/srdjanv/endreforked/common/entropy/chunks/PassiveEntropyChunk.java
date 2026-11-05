package io.github.srdjanv.endreforked.common.entropy.chunks;

import java.util.function.IntSupplier;

import io.github.srdjanv.endreforked.api.entropy.world.EntropyChunkReader;
import io.github.srdjanv.endreforked.api.util.Ticker;

abstract class PassiveEntropyChunk {

    protected final EntropyChunkReader wrapper;
    protected final Ticker ticker;
    protected final IntSupplier entropy;

    PassiveEntropyChunk(EntropyChunkReader wrapper, Ticker ticker, IntSupplier entropy) {
        this.wrapper = wrapper;
        this.ticker = ticker;
        this.entropy = entropy;
    }
}
