package io.github.srdjanv.endreforked.common.enchants;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;

import io.github.srdjanv.endreforked.Tags;
import io.github.srdjanv.endreforked.common.ModEnchants;

public class EnchantEnderCore extends Enchantment {

    public EnchantEnderCore() {
        super(Rarity.RARE, EnumEnchantmentType.ARMOR_CHEST, new EntityEquipmentSlot[] { EntityEquipmentSlot.CHEST });
        setRegistryName(Tags.MODID, "ender_core");
        setName("ender_core");
    }

    public void onUserHurt(EntityLivingBase user, Entity attacker, int level) {
        if (!(user instanceof EntityPlayer wearer)) return;
        if (((wearer.getHealth() / wearer.getMaxHealth()) * 100) > 10) return;

        // TODO: 05/11/2023 check if its a good idea to add the item to the CooldownTracker
        Item chestPeace = null;
        for (ItemStack stack : ModEnchants.ender_core.getEntityEquipment(wearer)) {
            int chestLevel = EnchantmentHelper.getEnchantmentLevel(ModEnchants.ender_core, stack);
            if (chestLevel < 1) continue;

            var item = stack.getItem();
            if (wearer.getCooldownTracker().hasCooldown(item)) return;
            chestPeace = item;
            break;
        }

        if (chestPeace == null) return;
        for (int i = 0; i < 16; ++i) {
            double x = wearer.posX + boundRand(wearer) * 16.0D;
            double y = MathHelper.clamp(wearer.posY + (double) (wearer.getRNG().nextInt(16) - 8), 0.0D,
                    wearer.world.getActualHeight() - 1);
            double z = wearer.posZ + boundRand(wearer) * 16.0D;
            if (wearer.isRiding()) wearer.dismountRidingEntity();

            if (wearer.attemptTeleport(x, y, z)) {
                wearer.getCooldownTracker().setCooldown(chestPeace, 80);
                wearer.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 250));
                wearer.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }
    }

    public static double boundRand(EntityPlayer wearer) {
        while (true) {
            var rand = wearer.getRNG().nextDouble();
            if (rand > 0.6 || rand < 0.4) {
                return rand;
            }
        }
    }
}
