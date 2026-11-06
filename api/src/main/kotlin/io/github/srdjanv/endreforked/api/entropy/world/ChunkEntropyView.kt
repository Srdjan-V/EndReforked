package io.github.srdjanv.endreforked.api.entropy.world

import io.github.srdjanv.endreforked.api.capabilities.entropy.EntropyChunk
import io.github.srdjanv.endreforked.api.capabilities.entropy.WeakEntropyStorage
import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.api.util.DimPos
import java.util.Optional
import java.util.function.Function
import java.util.function.Supplier

class ChunkEntropyView(
    val centerPosSup: Supplier<DimPos>,
    val resolver: Function<DimPos, Optional<EntropyChunk>>,
    private val radius: EntropyRadius,
) : WeakEntropyStorage, EntropyDataProvider {
    private val sortedEntropies = ArrayList<EntropyChunk>()
    var centerEntropy: EntropyChunk? = null
        private set

    init {
        buildView()
    }

    override val entropyRadius: Optional<EntropyRadius> get() = Optional.of(radius)

    fun buildView() {
        val pos = centerPosSup.get() ?: return
        val center = centerEntropy
        if (center != null && center.dimPos == pos && sortedEntropies.size == radius.chunks && !validateView()) return

        val resolved = resolver.apply(pos).orElse(null)
        if (resolved == null) {
            sortedEntropies.clear()
            return
        }
        centerEntropy = resolved
        sortedEntropies.clear()
        for (chunk in radius.getChunksInRadius(resolved.dimPos)) resolver.apply(chunk).ifPresent { sortedEntropies += it }
        sortedEntropies.sortWith(compareBy<EntropyChunk> { it.isOverLoaded }.thenBy { it.currentEntropy })
    }

    fun validateView(): Boolean = sortedEntropies.all { it.isLoaded }

    val view: List<EntropyChunk>
        get() {
            buildView()
            return sortedEntropies
        }

    override var loadFactor: Double
        get() = view.map { it.loadFactor }.average().let { if (it.isNaN()) 0.0 else it }
        set(value) = view.forEach { it.loadFactor = value }

    override val isOverLoaded: Boolean get() = view.any { it.isOverLoaded }

    override var decay: Int
        get() = view.map { it.decay }.average().let { if (it.isNaN()) 0 else it.toInt() }
        set(value) = view.forEach { it.decay = value }

    override val maxEntropy: Int get() = view.sumOf { it.maxEntropy }
    override val currentEntropy: Int get() = view.sumOf { it.currentEntropy }

    override fun induceEntropy(entropy: Int, simulate: Boolean) = distribute(entropy, simulate) { chunk, left -> chunk.induceEntropy(left, simulate) }

    override fun drainEntropy(entropy: Int, simulate: Boolean) = distribute(entropy, simulate) { chunk, left -> chunk.drainEntropy(left, simulate) }

    /** Offers [entropy] to each chunk in order, then drains what is left from the chunk references. Returns what was accepted. */
    private inline fun distribute(entropy: Int, simulate: Boolean, first: (EntropyChunk, Int) -> Int): Int {
        buildView()
        var left = entropy
        for (chunk in sortedEntropies) {
            if (left <= 0) break
            left -= first(chunk, left)
        }
        for (chunk in sortedEntropies) {
            if (left <= 0) break
            chunk.entropyStorageReference.ifPresent { left -= it.drainEntropy(left, simulate) }
        }
        return entropy - left
    }
}
