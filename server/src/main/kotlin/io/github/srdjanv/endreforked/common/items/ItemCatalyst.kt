package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.items.base.ItemBase
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.EnumRarity
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemCatalyst(name: String) : ItemBase(name) {
    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        tooltip.add(I18n.format("tile.catalyst.tooltip"))
    }

    override fun getRarity(stack: ItemStack) = EnumRarity.RARE
}
