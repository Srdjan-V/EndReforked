package io.github.srdjanv.endreforked.common.tiles.passiveinducers

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.tiles.base.BasePassiveInducer
import net.minecraft.util.EnumParticleTypes

class EntropyCropTile : BasePassiveInducer(EntropyRadius.TWO, 5 * 20, 25, particleColor = 0x5900b3, particleType = EnumParticleTypes.SPELL)
