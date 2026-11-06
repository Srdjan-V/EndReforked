package io.github.srdjanv.endreforked.compat.tconstruct

import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.util.text.TextFormatting
import slimeknights.tconstruct.library.materials.Material
import slimeknights.tconstruct.library.traits.AbstractTrait

object Wolframium {
    @JvmField val material = Material("wolframium", 0x2F332A)

    @JvmField val trait = object : AbstractTrait("wolframium", TextFormatting.GRAY) {
        override fun onHit(tool: ItemStack, player: EntityLivingBase, target: EntityLivingBase, damage: Float, isCritical: Boolean) {
            if (player.world.currentMoonPhaseFactor == 1.0f) player.addPotionEffect(PotionEffect(MobEffects.STRENGTH, 256, 1))
        }
    }
}
