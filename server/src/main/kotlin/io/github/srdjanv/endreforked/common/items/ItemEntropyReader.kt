package io.github.srdjanv.endreforked.common.items

import io.github.srdjanv.endreforked.api.entropy.EntropyDataProvider
import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import io.github.srdjanv.endreforked.common.items.base.ItemBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World

class ItemEntropyReader : ItemBase("entropy_reader") {
    override fun onItemUse(player: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing,
                           hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        if (!worldIn.isRemote && player.isSneaking) {
            (worldIn.getTileEntity(pos) as? EntropyDataProvider)?.formatedEntropyData?.ifPresent { lines ->
                lines.forEach { player.sendMessage(TextComponentString(it)) }
            }
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ)
    }

    override fun onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult<ItemStack> {
        if (!worldIn.isRemote && !playerIn.isSneaking) {
            val reader = EntropyChunkReader.ofEntity(playerIn, EntropyRadius.ONE)
            for (entry in reader.entropyView.view) {
                playerIn.sendMessage(TextComponentString(entry.dimPos.toString()))
                entry.formatedEntropyData.ifPresent { lines ->
                    lines.forEach { playerIn.sendMessage(TextComponentString(it)) }
                }
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn)
    }
}
