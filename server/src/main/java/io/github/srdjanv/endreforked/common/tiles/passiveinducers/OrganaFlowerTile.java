package io.github.srdjanv.endreforked.common.tiles.passiveinducers;

import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.util.EnumParticleTypes;

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.common.tiles.base.BasePassiveInducer;

public class OrganaFlowerTile extends BasePassiveInducer {

    private int tick;

    public OrganaFlowerTile() {
        super(EntropyRadius.TWO, 5 * 20, 10, 60);
        this.tick = ThreadLocalRandom.current().nextInt(60);
    }

    @Override
    protected void particles() {
        spawnParticles(0x5900b3, EnumParticleTypes.SPELL_MOB);
    }
}
