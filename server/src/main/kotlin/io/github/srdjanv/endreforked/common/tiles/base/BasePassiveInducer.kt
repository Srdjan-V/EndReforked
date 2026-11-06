package io.github.srdjanv.endreforked.common.tiles.base

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.api.util.Ticker
import io.github.srdjanv.endreforked.common.entropy.chunks.PassiveEntropyChunkInducer
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ITickable
import java.util.Optional

abstract class BasePassiveInducer(
    private val range: EntropyRadius,
    frequency: Int,
    entropy: Int,
    particleFrequency: Int = frequency,
    private val particleColor: Int,
    private val particleType: EnumParticleTypes,
) : BaseTileEntity(), ITickable, EntropyDataProvider {
    private val inducer = PassiveEntropyChunkInducer(EntropyChunkReader.ofTileEntity(this, range), Ticker(frequency), entropy)
    private val particleTicker = Ticker(particleFrequency)

    override val entropyRadius: Optional<EntropyRadius> get() = Optional.of(range)

    override val passiveInducer: Optional<EntropyDataProvider.PassiveInducer> get() = Optional.of(inducer)

    override fun update() {
        inducer.induce()
        if (world.isRemote && particleTicker.tick()) {
            world.spawnParticle(
                particleType,
                pos.x + (world.rand.nextDouble() - 0.5),
                pos.y + (world.rand.nextDouble() - 0.5),
                pos.z + (world.rand.nextDouble() - 0.5),
                (particleColor shr 16 and 255) / 255.0,
                (particleColor shr 8 and 255) / 255.0,
                (particleColor and 255) / 255.0,
            )
        }
    }
}
