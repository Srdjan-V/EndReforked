package io.github.srdjanv.endreforked.common.potions.base;

import net.minecraft.potion.Potion;

public class BasePotion extends Potion {

    public BasePotion(String name, boolean isBadEffectIn, int liquidColorIn) {
        super(isBadEffectIn, liquidColorIn);
        setRegistryName(name);
        setPotionName("effect." + name);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}
