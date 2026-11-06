package io.github.srdjanv.endreforked.common.tiles.passiveinducers

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.fluids.FluidOrgana
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.tiles.base.BasePassiveInducer
import net.minecraft.util.EnumParticleTypes

class FluidEntropyTile : BasePassiveInducer(EntropyRadius.TWO, EntropyTuning.fluidEntropy, particleFrequency = 60, particleColor = FluidOrgana.COLOUR, particleType = EnumParticleTypes.SPELL)
