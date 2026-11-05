package io.github.srdjanv.endreforked.common.tiles.passiveinducers;

import net.minecraft.util.EnumParticleTypes;

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.common.tiles.base.BasePassiveInducer;

public class OrganaWeedTile extends BasePassiveInducer {

    public OrganaWeedTile() {
        super(EntropyRadius.ONE, 5 * 20, 5);
    }

    @Override
    protected void particles() {
        spawnParticles(0x5900b3, EnumParticleTypes.SPELL_MOB);
    }
}
