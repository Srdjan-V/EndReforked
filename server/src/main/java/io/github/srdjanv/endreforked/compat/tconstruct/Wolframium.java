package io.github.srdjanv.endreforked.compat.tconstruct;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;

import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public final class Wolframium {

    public static final Material material = new Material("wolframium", 0x2F_33_2A);

    public static final AbstractTrait trait = new AbstractTrait("wolframium", TextFormatting.GRAY) {

        @Override
        public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage,
                          boolean isCritical) {
            if (player.world.getCurrentMoonPhaseFactor() == 1.0F)
                player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 256, 1));
        }
    };

    private Wolframium() {}
}
