package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.items.base.ItemBase
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.EnumRarity
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemWorldMirror(name: String) : ItemBase(name) {
    init {
        setMaxStackSize(1)
    }

    override fun onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult<ItemStack> {
        playerIn.heldItemMainhand.damageItem(1, playerIn)
        playerIn.cooldownTracker.setCooldown(this, 1000)
        val spawn = worldIn.provider.randomizedSpawnPoint
        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT,
            SoundCategory.PLAYERS, 0.5f, worldIn.rand.nextFloat() * 0.1f + 0.9f)
        playerIn.setPositionAndUpdate(spawn.x.toDouble(), spawn.y.toDouble(), spawn.z.toDouble())
        return ActionResult(EnumActionResult.PASS, playerIn.getHeldItem(handIn))
    }

    override fun getMaxItemUseDuration(stack: ItemStack) = 72000

    override fun getRarity(stack: ItemStack) = EnumRarity.RARE

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        tooltip.add(I18n.format("tile.mirror.tooltip_0"))
        tooltip.add(I18n.format("tile.mirror.tooltip_1"))
    }
}
