package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.EnumRarity
import net.minecraft.item.Item.ToolMaterial
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.stats.StatList
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemEnderSword(name: String, material: ToolMaterial) : ItemSword(material), InventoryItemModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setCreativeTab(EndReforked.ENDERTAB)
    }

    override fun onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult<ItemStack> {
        val stack = playerIn.getHeldItem(handIn)
        if (!playerIn.capabilities.isCreativeMode) stack.damageItem(4, playerIn)
        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ENDERPEARL_THROW,
            SoundCategory.NEUTRAL, 0.5f, 0.4f / (itemRand.nextFloat() * 0.4f + 0.8f))
        playerIn.cooldownTracker.setCooldown(this, 20)
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
        tooltip.add(I18n.format("tile.ender_sword.tooltip"))
    }

    override fun getRarity(stack: ItemStack) = EnumRarity.RARE
}
