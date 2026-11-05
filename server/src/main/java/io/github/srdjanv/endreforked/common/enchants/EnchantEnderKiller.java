package io.github.srdjanv.endreforked.common.enchants;

import java.util.Arrays;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.PotionEffect;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.configs.Configs;

public class EnchantEnderKiller extends Enchantment {

    public EnchantEnderKiller() {
        super(Rarity.RARE, EnumEnchantmentType.WEAPON, new EntityEquipmentSlot[] { EntityEquipmentSlot.MAINHAND });
        setRegistryName(Tags.MODID, "ender_killer");
        setName("ender_killer");
    }

    public void onEntityDamaged(EntityLivingBase user, Entity target, int level) {
        if (!(target instanceof EntityLivingBase targetMob)) return;
        if (Arrays.stream(Configs.SERVER_SIDE_CONFIGS.enchantBoost).anyMatch(i -> i == user.dimension))
            targetMob.addPotionEffect(new PotionEffect(MobEffects.INSTANT_DAMAGE, 5, level));
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }
}
