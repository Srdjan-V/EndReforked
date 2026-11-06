package io.github.srdjanv.endreforked.common.entity

import io.github.srdjanv.endreforked.common.ModBlocks
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World
import net.minecraftforge.event.ForgeEventFactory

class EntityEntropyFireball : EntityFireball {
    constructor(world: World) : super(world) {
        setSize(0.3125f, 0.3125f)
    }

    constructor(world: World, shooter: EntityLivingBase, accelX: Double, accelY: Double, accelZ: Double) :
        super(world, shooter, accelX, accelY, accelZ) {
        setSize(0.3125f, 0.3125f)
    }

    constructor(world: World, x: Double, y: Double, z: Double, accelX: Double, accelY: Double, accelZ: Double) :
        super(world, x, y, z, accelX, accelY, accelZ) {
        setSize(0.3125f, 0.3125f)
    }

    override fun onImpact(result: RayTraceResult) {
        if (world.isRemote) return
        val hit = result.entityHit
        if (hit != null) {
            if (!hit.isImmuneToFire && hit.attackEntityFrom(DamageSource.causeFireballDamage(this, shootingEntity), 5f)) {
                applyEnchantments(shootingEntity, hit)
                hit.setFire(2)
            }
        } else if (shootingEntity !is EntityLiving || ForgeEventFactory.getMobGriefingEvent(world, shootingEntity)) {
            val pos = result.blockPos.offset(result.sideHit)
            if (world.isAirBlock(pos)) world.setBlockState(pos, ModBlocks.ENTROPY_FIRE.get().defaultState)
        }
        setDead()
    }

    override fun canBeCollidedWith() = false

    override fun attackEntityFrom(source: DamageSource, amount: Float) = false

    override fun getParticleType() = EnumParticleTypes.PORTAL

    override fun isFireballFiery() = false
}
