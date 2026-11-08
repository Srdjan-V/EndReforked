package io.github.srdjanv.endreforked.common.enchants

import io.github.srdjanv.endreforked.Tags
import io.github.srdjanv.endreforked.common.configs.Configs
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnumEnchantmentType
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.MobEffects
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.potion.PotionEffect

class EnchantEnderKiller : Enchantment(Rarity.RARE, EnumEnchantmentType.WEAPON, arrayOf(EntityEquipmentSlot.MAINHAND)) {
    init {
        setRegistryName(Tags.MODID, "ender_killer")
        setName("ender_killer")
    }

    override fun onEntityDamaged(user: EntityLivingBase, target: Entity, level: Int) {
        if (target is EntityLivingBase && user.dimension in Configs.SERVER_SIDE_CONFIGS.enchantBoost)
            target.addPotionEffect(PotionEffect(MobEffects.INSTANT_DAMAGE, 5, level))
    }

    override fun getMaxLevel() = 2
}
