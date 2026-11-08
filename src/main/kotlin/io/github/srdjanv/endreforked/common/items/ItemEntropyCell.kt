package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.entropy.EntropyCell
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import io.github.srdjanv.endreforked.common.items.base.ItemBase
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/** Carries entropy in its NBT. Use fills it from the chunk, sneak + use empties it into the chunk. */
class ItemEntropyCell : ItemBase("entropy_cell") {
    init {
        maxStackSize = 1
    }

    override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack> {
        val stack = player.getHeldItem(hand)
        if (world.isRemote) return ActionResult(EnumActionResult.SUCCESS, stack)
        val view = EntropyChunkReader.ofEntity(player, EntropyRadius.ONE).entropyView
        val content = content(stack)
        val moved: Int
        if (player.isSneaking) {
            moved = EntropyCell.empty(content, view.induceEntropy(content, true))
            if (moved > 0) view.induceEntropy(moved, false)
            setContent(stack, content - moved)
        } else {
            val want = EntropyCell.fill(content, EntropyTuning.cellCapacity, EntropyTuning.cellCapacity)
            moved = if (want > 0) view.drainEntropy(want, false) else 0
            setContent(stack, content + moved)
        }
        val key = if (player.isSneaking) "entropy_cell.emptied" else "entropy_cell.filled"
        player.sendStatusMessage(TextComponentTranslation(key, moved, content(stack), EntropyTuning.cellCapacity), true)
        return ActionResult(EnumActionResult.SUCCESS, stack)
    }

    override fun showDurabilityBar(stack: ItemStack) = content(stack) > 0

    /** The bar shows the missing part of the capacity, so a full cell has a full bar. */
    override fun getDurabilityForDisplay(stack: ItemStack) = 1.0 - content(stack).toDouble() / EntropyTuning.cellCapacity

    override fun getRGBDurabilityForDisplay(stack: ItemStack) = 0x9a3de8

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, world: World?, tooltip: MutableList<String>, flag: ITooltipFlag) {
        tooltip.add(I18n.format("entropy_cell.tooltip", content(stack), EntropyTuning.cellCapacity))
    }

    companion object {
        const val KEY = "entropy"

        fun content(stack: ItemStack): Int = if (stack.item is ItemEntropyCell) stack.tagCompound?.getInteger(KEY) ?: 0 else 0

        private fun setContent(stack: ItemStack, value: Int) {
            stack.setTagInfo(KEY, net.minecraft.nbt.NBTTagInt(value))
        }

        /** Takes [amount] from [stack] if it is a cell that holds that much. */
        fun take(stack: ItemStack, amount: Int): Boolean {
            if (content(stack) < amount) return false
            setContent(stack, content(stack) - amount)
            return true
        }
    }
}
