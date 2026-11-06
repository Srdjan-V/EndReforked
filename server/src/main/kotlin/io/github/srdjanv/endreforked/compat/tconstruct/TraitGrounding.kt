package io.github.srdjanv.endreforked.compat.tconstruct

import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.entropy.world.EntropyWorldHandler
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import slimeknights.tconstruct.library.traits.AbstractTrait

/**
 * Grounding: a hit target cannot teleport for 10 s. Tinkers' own Enderference does nearly the same, so the entropy link is the difference:
 * the time is longer in a charged (x1.5) and in a critical (x2) chunk of the target. The expiry is in the entity data.
 */
object TraitGrounding : AbstractTrait("endreborn_grounding", TextFormatting.GRAY) {
    private const val KEY = "endreborn_grounded_until"
    private const val BASE_TICKS = 10 * 20

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun onHit(tool: ItemStack, player: EntityLivingBase, target: EntityLivingBase, damage: Float, isCritical: Boolean) {
        if (target.world.isRemote) return
        val level = EntropyWorldHandler.levelAt(target.world.provider.dimension, target.position)
        target.entityData.setLong(KEY, target.world.totalWorldTime + (BASE_TICKS * EntropyTuning.speed(level)).toLong())
    }

    @SubscribeEvent
    fun onTeleport(event: EnderTeleportEvent) {
        val e = event.entityLiving
        if (e.entityData.getLong(KEY) > e.world.totalWorldTime) event.isCanceled = true
    }
}
