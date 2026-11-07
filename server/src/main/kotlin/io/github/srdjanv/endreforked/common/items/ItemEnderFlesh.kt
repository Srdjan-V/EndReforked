package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.ModPotions
import io.github.srdjanv.endreforked.common.items.base.ItemFoodBase
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemEnderFlesh : ItemFoodBase("ender_flesh", 2, 0.4f, false) {
    init {
        setAlwaysEdible()
    }

    /** Applied here, not through setPotionEffect: potions are registered after items, so the potion does not exist at construction. */
    override fun onFoodEaten(stack: ItemStack, world: World, player: EntityPlayer) {
        if (!world.isRemote) player.addPotionEffect(PotionEffect(ModPotions.ENDER_EYES, 90 * 20, 0))
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        tooltip.add(I18n.format("tile.flesh.tooltip"))
    }
}
