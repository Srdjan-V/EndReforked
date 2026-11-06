package io.github.srdjanv.endreforked.common.tiles.passiveinducers

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.tiles.base.BasePassiveInducer
import net.minecraft.util.EnumParticleTypes

class EntropyCropTile : BasePassiveInducer(EntropyRadius.TWO, EntropyTuning.entropyCrop, particleColor = 0x5900b3, particleType = EnumParticleTypes.SPELL)
