package io.github.srdjanv.endreforked.common.tiles.passiveinducers

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.tiles.base.BasePassiveInducer
import net.minecraft.util.EnumParticleTypes

class OrganaWeedTile : BasePassiveInducer(EntropyRadius.ONE, 5 * 20, 5, particleColor = 0x5900b3, particleType = EnumParticleTypes.SPELL_MOB)
