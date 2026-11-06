package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.common.items.tools.ToolSword
import io.github.srdjanv.endreforked.utils.ItemStackUtils
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.EnumRarity
import net.minecraft.item.Item.ToolMaterial
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ItemDeather(name: String, material: ToolMaterial) : ToolSword(name, material) {
    override fun getRarity(stack: ItemStack) = EnumRarity.UNCOMMON

    override fun itemInteractionForEntity(stack: ItemStack, player: EntityPlayer, target: EntityLivingBase,
                                          hand: EnumHand): Boolean {
        if (player.world.isRemote) return false
        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT,
            SoundCategory.PLAYERS, 0.2f, player.world.rand.nextFloat() * 0.1f + 0.9f)
        val dir = player.positionVector.subtract(target.positionVector)
        when (getMode(stack)) {
            0 -> target.addVelocity(dir.x * 0.3, dir.y * 0.3, dir.z * 0.3)
            1 -> target.addVelocity(dir.x * -0.3, dir.y * -0.3, dir.z * -0.3)
        }
        if (!player.capabilities.isCreativeMode) stack.damageItem(1, player)
        return true
    }

    override fun onItemRightClick(world: World, entityplayer: EntityPlayer, hand: EnumHand): ActionResult<ItemStack> {
        val stack = entityplayer.getHeldItem(hand)
        if (!entityplayer.isSneaking) return ActionResult(EnumActionResult.PASS, stack)
        if (!world.isRemote) {
            toggleMode(stack)
            world.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.BLOCK_LEVER_CLICK,
                SoundCategory.PLAYERS, 0.5f, world.rand.nextFloat() * 0.1f + 0.9f)
        }
        return ActionResult(EnumActionResult.SUCCESS, stack)
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        tooltip.add(I18n.format("tile.deather.tooltip"))
        tooltip.add(I18n.format("tile.deather_mode.tooltip") + ": " + getModeName(stack))
    }

    fun getMode(itemStack: ItemStack) = ItemStackUtils.getInt(itemStack, "mode")

    fun getModeName(itemStack: ItemStack): String? = when (getMode(itemStack)) {
        0 -> I18n.format("tile.deather_mode.off")
        1 -> I18n.format("tile.deather_mode.on")
        else -> null
    }

    fun toggleMode(itemStack: ItemStack) =
        ItemStackUtils.setInt(itemStack, "mode", if (getMode(itemStack) < 1) getMode(itemStack) + 1 else 0)
}
