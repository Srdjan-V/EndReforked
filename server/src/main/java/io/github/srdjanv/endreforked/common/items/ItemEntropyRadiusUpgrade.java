package io.github.srdjanv.endreforked.common.items;

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius;
import io.github.srdjanv.endreforked.api.entropy.EntropyRadiusUpgrade;
import io.github.srdjanv.endreforked.common.items.base.ItemBase;

public class ItemEntropyRadiusUpgrade extends ItemBase implements EntropyRadiusUpgrade {

    private final EntropyRadius radius;

    public ItemEntropyRadiusUpgrade(EntropyRadius radius) {
        super("entropy_radius_upgrade_" + radius.ordinal());
        this.radius = radius;
        setMaxStackSize(1);
    }

    @Override
    public EntropyRadius getEntropyRadius() {
        return radius;
    }
}
