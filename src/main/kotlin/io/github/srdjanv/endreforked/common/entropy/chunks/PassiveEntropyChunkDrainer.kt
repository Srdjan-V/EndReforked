package io.github.srdjanv.endreforked.common.entropy.chunks

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.api.util.Ticker
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import java.util.OptionalInt

class PassiveEntropyChunkDrainer(
    private val reader: EntropyChunkReader,
    private val ticker: Ticker,
    private val baseEntropy: () -> Int,
    private val radius: () -> EntropyRadius,
) : EntropyDataProvider.PassiveDrainer {
    override val frequency: OptionalInt = OptionalInt.of(ticker.frequency)
    override val drained get() = baseEntropy() * radius().chunks

    fun drain() {
        if (ticker.tick()) reader.entropyView.drainEntropy(drained, false)
    }
}
