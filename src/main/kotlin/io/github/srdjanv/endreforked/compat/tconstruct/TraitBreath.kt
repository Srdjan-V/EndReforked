package io.github.srdjanv.endreforked.compat.tconstruct

import io.github.srdjanv.endreforked.common.entropy.DragonBreathCloud
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.item.ItemStack
import net.minecraft.potion.PotionEffect
import net.minecraft.init.MobEffects
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.ProjectileImpactEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import slimeknights.tconstruct.library.traits.AbstractTrait
import slimeknights.tconstruct.library.utils.TagUtil
import slimeknights.tconstruct.library.utils.TinkerUtil

/**
 * Breath (bow string): a fully drawn arrow (Tinkers' sets isCritical at full draw) is tagged when it is shot.
 * When it hits, it leaves a dragon breath cloud (radius 2, 3 s, harm) that does not hit the shooter.
 */
object TraitBreath : AbstractTrait("endreborn_breath", TextFormatting.DARK_PURPLE) {
    private const val TAG = "endreborn_breath"

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    private fun hasBreath(stack: ItemStack) = !stack.isEmpty && TinkerUtil.hasTrait(TagUtil.getTagSafe(stack), identifier)

    @SubscribeEvent
    fun onShoot(event: EntityJoinWorldEvent) {
        val arrow = event.entity as? EntityArrow ?: return
        val shooter = arrow.shootingEntity as? EntityLivingBase ?: return
        if (event.world.isRemote || !arrow.isCritical) return
        if (hasBreath(shooter.activeItemStack) || hasBreath(shooter.heldItemMainhand) || hasBreath(shooter.heldItemOffhand)) arrow.addTag(TAG)
    }

    @SubscribeEvent
    fun onImpact(event: ProjectileImpactEvent) {
        val arrow = event.entity as? EntityArrow ?: return
        if (arrow.world.isRemote || !arrow.removeTag(TAG)) return
        val hit = event.rayTraceResult
        val pos = if (hit.typeOfHit == RayTraceResult.Type.BLOCK) hit.blockPos.up() else BlockPos(hit.hitVec)
        DragonBreathCloud.spawn(arrow.world, pos, 2f, 60, arrow.shootingEntity as? EntityLivingBase, true, PotionEffect(MobEffects.INSTANT_DAMAGE))
    }
}
