package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.capabilities.timedflight.CapabilityTimedFlightHandler
import io.github.srdjanv.endreforked.common.items.base.ItemFoodBase
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.init.PotionTypes
import net.minecraft.item.EnumAction
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.potion.PotionUtils
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemDragoniteTea : ItemFoodBase("dragonite_tea", 2, 1f, false) {
    init {
        setAlwaysEdible()
        setMaxStackSize(16)
    }

    override fun onItemUseFinish(stack: ItemStack, worldIn: World, entityLiving: EntityLivingBase): ItemStack {
        super.onItemUseFinish(stack, worldIn, entityLiving)
        if (entityLiving is EntityPlayer) {
            entityLiving.cooldownTracker.setCooldown(this, 40 * 20)
            entityLiving.addPotionEffect(PotionEffect(MobEffects.HUNGER, 50 * 20, 0))
            if (!worldIn.isRemote) {
                entityLiving.getCapability(CapabilityTimedFlightHandler.INSTANCE, null)!!.flightDuration = 45 * 20
            }
        }
        return stack
    }

    override fun getMaxItemUseDuration(stack: ItemStack) = 10

    @SideOnly(Side.CLIENT)
    override fun getDefaultInstance(): ItemStack =
        PotionUtils.addPotionToItemStack(super.getDefaultInstance(), PotionTypes.HEALING)

    override fun getItemUseAction(stack: ItemStack) = EnumAction.DRINK

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        tooltip.add(I18n.format("tile.tea.tooltip"))
    }
}
