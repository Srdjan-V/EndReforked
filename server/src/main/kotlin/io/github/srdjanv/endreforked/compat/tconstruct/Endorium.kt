package io.github.srdjanv.endreforked.compat.tconstruct

import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.util.text.TextFormatting
import slimeknights.tconstruct.library.materials.Material
import slimeknights.tconstruct.library.traits.AbstractTrait

object Endorium {
    @JvmField val material = Material("endorium", 0x1b7b6b)

    @JvmField val trait = object : AbstractTrait("endorium", TextFormatting.GRAY) {
        override fun onHit(tool: ItemStack, player: EntityLivingBase, target: EntityLivingBase, damage: Float, isCritical: Boolean) {
            if (isCritical) player.addPotionEffect(PotionEffect(MobEffects.HASTE, 120, 0))
        }
    }
}
