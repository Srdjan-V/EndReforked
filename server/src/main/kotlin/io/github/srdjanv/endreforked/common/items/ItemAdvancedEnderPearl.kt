package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.items.base.ItemBase
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemAdvancedEnderPearl(name: String) : ItemBase(name) {
    init {
        maxStackSize = 32
    }

    override fun onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult<ItemStack> {
        val stack = playerIn.getHeldItem(handIn)
        if (!playerIn.capabilities.isCreativeMode) stack.shrink(1)
        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ENDERPEARL_THROW,
            SoundCategory.NEUTRAL, 0.5f, 0.4f / (itemRand.nextFloat() * 0.4f + 0.8f))
        playerIn.cooldownTracker.setCooldown(this, 10)
        if (!worldIn.isRemote) {
            val pearl = EntityEnderPearl(worldIn, playerIn)
            pearl.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0f, 1.5f, 1f)
            worldIn.spawnEntity(pearl)
        }
        playerIn.addStat(StatList.getObjectUseStats(this)!!)
        return ActionResult(EnumActionResult.SUCCESS, stack)
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        tooltip.add(I18n.format("tile.a_pearl.tooltip"))
    }
}
