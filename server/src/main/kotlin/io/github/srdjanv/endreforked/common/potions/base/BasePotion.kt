package io.github.srdjanv.endreforked.common.potions.base

import net.minecraft.potion.Potion

open class BasePotion(name: String, isBadEffect: Boolean, liquidColor: Int) : Potion(isBadEffect, liquidColor) {
    init {
        setRegistryName(name)
        setPotionName("effect.$name")
    }

    override fun isReady(duration: Int, amplifier: Int) = true
}
