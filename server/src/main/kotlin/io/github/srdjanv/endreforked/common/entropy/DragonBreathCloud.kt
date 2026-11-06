package io.github.srdjanv.endreforked.common.entropy

import net.minecraft.entity.EntityLivingBase
import net.minecraft.init.MobEffects
import net.minecraft.potion.PotionEffect
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * The one lingering dragon breath cloud of the mod: the critical event Burst and the Breath trait use it.
 * With [excludeOwner] the owner is never hit. The vanilla cloud cannot do that, so this one applies [effect] itself
 * (the vanilla part has no effect and only does the shrinking, the timer and the particles).
 * The clients see a vanilla cloud. A cloud is not saved with its chunk. // ponytail: the entity is not registered, so it is lost on unload
 */
object DragonBreathCloud {
    private class Cloud(world: World, private val effect: PotionEffect, private val excludeOwner: Boolean) : net.minecraft.entity.EntityAreaEffectCloud(world) {
        private val delays = HashMap<EntityLivingBase, Int>()

        override fun onUpdate() {
            super.onUpdate()
            if (world.isRemote || isDead || ticksExisted % 5 != 0) return
            delays.entries.removeIf { it.value <= ticksExisted }
            for (e in world.getEntitiesWithinAABB(EntityLivingBase::class.java, entityBoundingBox)) {
                if (!e.canBeHitWithPotion() || (excludeOwner && e === owner) || e in delays) continue
                val dx = e.posX - posX
                val dz = e.posZ - posZ
                if (dx * dx + dz * dz > radius * radius) continue
                delays[e] = ticksExisted + 20
                if (effect.potion.isInstant) effect.potion.affectEntity(this, owner, e, effect.amplifier, 0.5) else e.addPotionEffect(PotionEffect(effect))
            }
        }
    }

    @JvmStatic
    @JvmOverloads
    fun spawn(
        world: World, pos: BlockPos, radius: Float, durationTicks: Int,
        owner: EntityLivingBase? = null, excludeOwner: Boolean = false, effect: PotionEffect = PotionEffect(MobEffects.INSTANT_DAMAGE),
    ) {
        val cloud = Cloud(world, effect, excludeOwner)
        cloud.setPosition(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
        cloud.owner = owner
        cloud.setParticle(EnumParticleTypes.DRAGON_BREATH)
        cloud.radius = radius
        cloud.duration = durationTicks
        cloud.setRadiusPerTick(-radius / durationTicks)
        cloud.setColor(0xA020F0) // potion color of the harm effect would be red; the particle is purple
        world.spawnEntity(cloud)
    }
}
