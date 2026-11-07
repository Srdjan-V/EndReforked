package io.github.srdjanv.endreforked.common.tiles.base

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.api.util.Ticker
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.entropy.chunks.PassiveEntropyChunkInducer
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import io.github.srdjanv.endreforked.utils.renderParticle
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ITickable
import java.util.Optional

abstract class BasePassiveInducer(
    private val range: EntropyRadius,
    tuning: EntropyTuning.Inducer,
    particleFrequency: Int = tuning.interval,
    private val particleColor: Int,
    private val particleType: EnumParticleTypes,
) : BaseTileEntity(), ITickable, EntropyDataProvider {
    private val inducer = PassiveEntropyChunkInducer(EntropyChunkReader.ofTileEntity(this, range), Ticker { tuning.interval }) { tuning.amount }
    private val particleTicker = Ticker(particleFrequency)

    override val entropyRadius: Optional<EntropyRadius> get() = Optional.of(range)

    override val passiveInducer: Optional<EntropyDataProvider.PassiveInducer> get() = Optional.of(inducer)

    override fun update() {
        inducer.induce()
        if (world.isRemote && particleTicker.tick()) {
            renderParticle(pos, world, particleType, particleColor)
        }
    }
}
