package io.github.srdjanv.endreforked.common.entropy.chunks

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.util.Ticker
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import java.util.OptionalInt

class PassiveEntropyChunkInducer(
    private val reader: EntropyChunkReader,
    private val ticker: Ticker,
    private val amount: () -> Int,
) : EntropyDataProvider.PassiveInducer {
    override val induced get() = amount()
    override val frequency: OptionalInt = OptionalInt.of(ticker.frequency)

    fun induce() {
        if (ticker.tick()) reader.entropyView.induceEntropy(induced, false)
    }
}
