package io.github.srdjanv.endreforked.compat.tconstruct

import io.github.srdjanv.endreforked.api.entropy.EntropyRadius
import io.github.srdjanv.endreforked.common.entropy.EntropyTuning
import io.github.srdjanv.endreforked.common.entropy.world.EntropyChunkReader
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.event.entity.player.PlayerEvent
import slimeknights.tconstruct.library.traits.AbstractTrait
import slimeknights.tconstruct.library.utils.ToolHelper

/**
 * Siphon: a broken block drains entropy from the chunk of the player (radius one).
 * Each drained point repairs the tool and adds mining speed for a short time. The bonus is in the entity data of the player.
 */
object TraitSiphon : AbstractTrait("endreborn_siphon", TextFormatting.LIGHT_PURPLE) {
    private const val POINTS = "endreborn_siphon_points"
    private const val UNTIL = "endreborn_siphon_until"

    override fun afterBlockBreak(tool: ItemStack, world: World, state: IBlockState, pos: BlockPos, entity: EntityLivingBase, wasEffective: Boolean) {
        if (world.isRemote || entity !is EntityPlayer) return
        val drained = EntropyChunkReader.ofEntity(entity, EntropyRadius.ONE).entropyView.drainEntropy(EntropyTuning.siphonDrain, false)
        if (drained <= 0) return
        val repair = drained / EntropyTuning.siphonEntropyPerDurability
        if (repair > 0) ToolHelper.healTool(tool, repair, entity)
        val data = entity.entityData
        val old = if (data.getLong(UNTIL) > world.totalWorldTime) data.getInteger(POINTS) else 0
        data.setInteger(POINTS, old + drained)
        data.setLong(UNTIL, world.totalWorldTime + EntropyTuning.siphonBonusTicks)
    }

    override fun miningSpeed(tool: ItemStack, event: PlayerEvent.BreakSpeed) {
        val player = event.entityPlayer
        val data = player.entityData
        if (data.getLong(UNTIL) <= player.world.totalWorldTime) return
        // ponytail: the bonus is capped at +100 %
        event.newSpeed *= 1f + (data.getInteger(POINTS) * EntropyTuning.siphonSpeedPerPoint).coerceAtMost(1.0).toFloat()
    }
}
