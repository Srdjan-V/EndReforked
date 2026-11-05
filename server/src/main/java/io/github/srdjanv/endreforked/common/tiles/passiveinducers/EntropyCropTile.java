package io.github.srdjanv.endreforked.common.tiles.passiveinducers;

import net.minecraft.util.EnumParticleTypes;

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.common.tiles.base.BasePassiveInducer;

public class EntropyCropTile extends BasePassiveInducer {

    public EntropyCropTile() {
        super(EntropyRadius.TWO, 5 * 20, 25);
    }

    @Override
    protected void particles() {
        // todo change color?
        spawnParticles(0x5900b3, EnumParticleTypes.SPELL);
    }
}
