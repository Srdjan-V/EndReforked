package io.github.srdjanv.endreforked.compat.tconstruct

import io.github.srdjanv.endreforked.compat.enderstorage.EnderStorageCompat
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.event.entity.living.LivingDropsEvent
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.items.wrapper.InvWrapper
import slimeknights.tconstruct.library.traits.AbstractTrait
import slimeknights.tconstruct.library.utils.TagUtil
import slimeknights.tconstruct.library.utils.TinkerUtil

/**
 * Ender Pocket: drops and XP of mined blocks and killed mobs go to the player. Overflow goes to the ender chest, then to the feet.
 * With Ender Storage loaded and an Ender Pouch in the inventory, the overflow goes to the ender chest of the pouch frequency.
 */
object TraitEnderPocket : AbstractTrait("endreborn_ender_pocket", TextFormatting.DARK_PURPLE) {
    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun hasEnderPocket(stack: ItemStack) = !stack.isEmpty && TinkerUtil.hasTrait(TagUtil.getTagSafe(stack), identifier)

    private fun give(player: EntityPlayer, stack: ItemStack) {
        // With Ender Storage and a pouch in the inventory, the pouch chest takes the place of the ender chest.
        val enderChest = (if (Loader.isModLoaded(EnderStorageCompat.MOD_ID)) EnderStorageCompat.pouchChest(player) else null)
            ?: InvWrapper(player.inventoryEnderChest)
        val rest = EnderPocketRouting.route(
            stack,
            listOf(
                { s -> ItemHandlerHelper.insertItemStacked(InvWrapper(player.inventory), s, false).takeUnless { it.isEmpty } },
                { s -> ItemHandlerHelper.insertItemStacked(enderChest, s, false).takeUnless { it.isEmpty } },
            ),
        )
        if (rest != null) player.dropItem(rest, false)
    }

    override fun blockHarvestDrops(tool: ItemStack, event: BlockEvent.HarvestDropsEvent) {
        val player = event.harvester ?: return
        if (event.world.isRemote || player is FakePlayer) return
        for (drop in event.drops) if (event.world.rand.nextFloat() <= event.dropChance) give(player, drop.copy())
        event.drops.clear()
    }

    override fun beforeBlockBreak(tool: ItemStack, event: BlockEvent.BreakEvent) {
        if (event.player is FakePlayer || event.expToDrop <= 0) return
        event.player.addExperience(event.expToDrop)
        event.expToDrop = 0
    }

    @SubscribeEvent
    fun onMobDrops(event: LivingDropsEvent) {
        val player = event.source.trueSource as? EntityPlayer ?: return
        if (player is FakePlayer || !hasEnderPocket(player.heldItemMainhand)) return
        for (drop in event.drops) give(player, drop.item.copy())
        event.drops.clear()
    }

    @SubscribeEvent
    fun onMobXp(event: LivingExperienceDropEvent) {
        val player = event.attackingPlayer ?: return
        if (player is FakePlayer || !hasEnderPocket(player.heldItemMainhand)) return
        player.addExperience(event.droppedExperience)
        event.droppedExperience = 0
    }
}
