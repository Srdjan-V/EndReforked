package io.github.srdjanv.endreforked.api.util;

import java.util.concurrent.ThreadLocalRandom;

public class Ticker {

    protected final int frequency;
    protected int tick;

    public int getFrequency() {
        return frequency;
    }

    public int getTick() {
        return tick;
    }

    public Ticker(int frequency) {
        this.frequency = frequency;
        tick = ThreadLocalRandom.current().nextInt(frequency);
    }

    public boolean tick() {
        return tick++ % frequency == 0;
    }
}
