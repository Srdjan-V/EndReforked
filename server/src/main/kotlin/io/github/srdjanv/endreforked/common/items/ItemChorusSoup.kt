package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.items.base.ItemFoodBase
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemChorusSoup : ItemFoodBase("chorus_soup", 5, 5f, false) {
    init {
        setMaxStackSize(1)
        setPotionEffect(PotionEffect(MobEffects.GLOWING, 100, 0), 0.6f)
    }

    override fun onItemUseFinish(stack: ItemStack, worldIn: World, entityLiving: EntityLivingBase): ItemStack {
        super.onItemUseFinish(stack, worldIn, entityLiving)
        return ItemStack(Items.BOWL)
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        tooltip.add(I18n.format("tile.soup.tooltip"))
    }
}
