package io.github.srdjanv.endreforked.common.tiles.base;

import java.util.Optional;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider;
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.api.entropy.world.EntropyChunkReader;
import io.github.srdjanv.endreforked.api.util.Ticker;
import io.github.srdjanv.endreforked.common.entropy.chunks.PassiveEntropyChunkInducer;

public abstract class BasePassiveInducer extends BaseTileEntity implements ITickable, EntropyDataProvider {

    private final EntropyRadius range;
    private final EntropyChunkReader tileWrapper;
    private final PassiveEntropyChunkInducer inducer;
    private final Ticker particleTicker;

    public BasePassiveInducer(EntropyRadius range, int frequency, int entropy) {
        this(range, frequency, entropy, frequency);
    }

    public BasePassiveInducer(EntropyRadius range, int frequency, int entropy, int particleFrequency) {
        this.range = range;
        this.tileWrapper = EntropyChunkReader.ofTileEntity(this, range);
        this.inducer = new PassiveEntropyChunkInducer(tileWrapper, new Ticker(frequency), entropy);
        particleTicker = new Ticker(particleFrequency);
    }

    @Override
    public Optional<EntropyRadius> getEntropyRadius() {
        return Optional.of(range);
    }

    @Override
    public Optional<PassiveInducer> getPassiveInducer() {
        return Optional.of(inducer.getPassiveInducer());
    }

    @Override
    public void update() {
        inducer.induce();
        if (world.isRemote && particleTicker.tick()) particles();
    }

    protected abstract void particles();

    protected void spawnParticles(int color, EnumParticleTypes type) {
        double xSpeed = (double) (color >> 16 & 255) / 255.0D;
        double ySpeed = (double) (color >> 8 & 255) / 255.0D;
        double zSpeed = (double) (color >> 0 & 255) / 255.0D;
        world.spawnParticle(type,
                pos.getX() + (world.rand.nextDouble() - 0.5D),
                pos.getY() + (world.rand.nextDouble() - 0.5D),
                pos.getZ() + (world.rand.nextDouble() - 0.5D),
                xSpeed, ySpeed, zSpeed);
    }
}
