package io.github.srdjanv.endreforked.common.items.tools

import io.github.srdjanv.endreforked.EndReforked
import io.github.srdjanv.endreforked.common.world.WandInteractions
import io.github.srdjanv.endreforked.utils.models.InventoryItemModel
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.item.Item.ToolMaterial
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class ToolEntropyWand(name: String, material: ToolMaterial) : ItemSword(material), InventoryItemModel {
    init {
        setTranslationKey(name)
        setRegistryName(name)
        setMaxDamage(128)
        setCreativeTab(EndReforked.ENDERTAB)
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        tooltip.add(I18n.format("tile.wand.tooltip"))
    }

    override fun onLeftClickEntity(stack: ItemStack, player: EntityPlayer, entity: Entity): Boolean {
        val world = player.world
        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_WITHER_SHOOT,
            SoundCategory.PLAYERS, 0.5f, world.rand.nextFloat() * 0.1f + 0.9f)
        entity.attackEntityFrom(DamageSource.WITHER, 7f)
        return true
    }

    override fun onItemUse(player: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing,
                           hitX: Float, hitY: Float, hitZ: Float): EnumActionResult {
        if (worldIn.isRemote) return EnumActionResult.PASS
        return if (WandInteractions.use(player, worldIn as WorldServer, pos, hand)) EnumActionResult.SUCCESS
        else EnumActionResult.PASS
    }
}
