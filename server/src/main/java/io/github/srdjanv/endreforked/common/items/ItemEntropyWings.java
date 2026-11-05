package io.github.srdjanv.endreforked.common.items;

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.api.entropy.EntropyWings;
import io.github.srdjanv.endreforked.common.items.base.ItemBase;

public class ItemEntropyWings extends ItemBase implements EntropyWings {

    public ItemEntropyWings() {
        super("entropy_wings");
    }

    public ItemEntropyWings(String name) {
        super(name);
    }

    @Override
    public int getEntropyCost() {
        return 250;
    }

    @Override
    public EntropyRadius getEntropyRadius() {
        return EntropyRadius.TWO;
    }

    @Override
    public int getFlightDuration() {
        return 25 * 20;
    }
}
