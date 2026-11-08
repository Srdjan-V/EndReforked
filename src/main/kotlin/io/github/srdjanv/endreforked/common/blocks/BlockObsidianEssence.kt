package io.github.srdjanv.endreforked.common.blocks

import io.github.srdjanv.endreforked.common.ModItems
import io.github.srdjanv.endreforked.common.blocks.base.BlockBase
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockObsidianEssence : BlockBase("essence_ore", Material.ROCK) {
    init {
        setSoundType(SoundType.STONE)
        setHardness(50.0F)
        setResistance(6000.0F)
        setLightLevel(1.0F)
        setHarvestLevel("pickaxe", 3)
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: World?, tooltip: MutableList<String>, advanced: ITooltipFlag) {
        tooltip.add(I18n.format("tile.essence.tooltip"))
    }

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = ModItems.END_ESSENCE
}
