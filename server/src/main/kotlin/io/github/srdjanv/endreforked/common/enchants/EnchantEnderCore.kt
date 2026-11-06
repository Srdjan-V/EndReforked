package io.github.srdjanv.endreforked.common.enchants

import io.github.srdjanv.endreforked.Tags
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnumEnchantmentType
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.init.SoundEvents
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.potion.PotionEffect
import net.minecraft.util.math.MathHelper

class EnchantEnderCore : Enchantment(Rarity.RARE, EnumEnchantmentType.ARMOR_CHEST, arrayOf(EntityEquipmentSlot.CHEST)) {
    init {
        setRegistryName(Tags.MODID, "ender_core")
        setName("ender_core")
    }

    override fun onUserHurt(user: EntityLivingBase, attacker: Entity, level: Int) {
        if (user !is EntityPlayer || user.health / user.maxHealth * 100 > 10) return

        // TODO: 05/11/2023 check if its a good idea to add the item to the CooldownTracker
        val chestPiece = getEntityEquipment(user)
            .firstOrNull { EnchantmentHelper.getEnchantmentLevel(this, it) >= 1 }?.item ?: return
        if (user.cooldownTracker.hasCooldown(chestPiece)) return

        repeat(16) {
            val x = user.posX + boundRand(user) * 16.0
            val y = MathHelper.clamp(user.posY + (user.rng.nextInt(16) - 8), 0.0, user.world.actualHeight - 1.0)
            val z = user.posZ + boundRand(user) * 16.0
            if (user.isRiding) user.dismountRidingEntity()
            if (user.attemptTeleport(x, y, z)) {
                user.cooldownTracker.setCooldown(chestPiece, 80)
                user.addPotionEffect(PotionEffect(MobEffects.REGENERATION, 250))
                user.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f)
                return
            }
        }
    }

    /** A random value that is never in the middle band 0.4..0.6. */
    private fun boundRand(wearer: EntityPlayer): Double {
        while (true) {
            val rand = wearer.rng.nextDouble()
            if (rand > 0.6 || rand < 0.4) return rand
        }
    }
}
