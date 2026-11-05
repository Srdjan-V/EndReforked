package io.github.srdjanv.endreforked.compat.tconstruct;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;

import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public final class Endorium {

    public static final Material material = new Material("endorium", 0x1b_7b_6b);

    public static final AbstractTrait trait = new AbstractTrait("endorium", TextFormatting.GRAY) {

        @Override
        public void onHit(ItemStack tool, EntityLivingBase player, EntityLivingBase target, float damage,
                          boolean isCritical) {
            if (isCritical)
                player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 120, isCritical ? 0 : 2));
        }
    };

    private Endorium() {}
}
