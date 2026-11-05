package io.github.srdjanv.endreforked.common.potions;

import io.github.srdjanv.endreforked.common.potions.base.BasePotion;

public class TimedFlight extends BasePotion {

    public TimedFlight() {
        super("timed_flight", false, 0x5900b3);
        setBeneficial();
    }
}
