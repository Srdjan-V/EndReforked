package io.github.srdjanv.endreforked.common.entropy.chunks

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.util.Ticker
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import java.util.OptionalInt

class PassiveEntropyChunkInducer(
    private val reader: EntropyChunkReader,
    private val ticker: Ticker,
    override val induced: Int,
) : EntropyDataProvider.PassiveInducer {
    override val frequency: OptionalInt = OptionalInt.of(ticker.frequency)

    fun induce() {
        if (ticker.tick()) reader.entropyView.induceEntropy(induced, false)
    }
}
