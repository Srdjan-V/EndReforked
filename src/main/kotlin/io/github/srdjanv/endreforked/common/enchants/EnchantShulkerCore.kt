package io.github.srdjanv.endreforked.common.enchants

import io.github.srdjanv.endreforked.Tags
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnumEnchantmentType
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.MobEffects
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.potion.PotionEffect

class EnchantShulkerCore : Enchantment(Rarity.RARE, EnumEnchantmentType.WEAPON, arrayOf(EntityEquipmentSlot.MAINHAND)) {
    init {
        setRegistryName(Tags.MODID, "shulker_core")
        setName("shulker_core")
    }

    override fun onEntityDamaged(user: EntityLivingBase, target: Entity, level: Int) {
        if (target is EntityLivingBase) target.addPotionEffect(PotionEffect(MobEffects.LEVITATION, 50, level + 2))
    }

    override fun getMaxLevel() = 3
}
