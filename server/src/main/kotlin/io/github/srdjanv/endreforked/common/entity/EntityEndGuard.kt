package io.github.srdjanv.endreforked.common.entity

import io.github.srdjanv.endreforked.common.LootHandler
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.*
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.pathfinding.PathNodeType
import net.minecraft.util.DamageSource
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class EntityEndGuard(world: World) : EntityMob(world) {
    companion object {
        private val ON_FIRE: DataParameter<Byte> = EntityDataManager.createKey(EntityEndGuard::class.java, DataSerializers.BYTE)
    }

    private var heightOffset = 0.5f
    private var heightOffsetUpdateTime = 0

    init {
        setPathPriority(PathNodeType.WATER, -1f)
        setPathPriority(PathNodeType.LAVA, 8f)
        setPathPriority(PathNodeType.DANGER_FIRE, 0f)
        setPathPriority(PathNodeType.DAMAGE_FIRE, 0f)
        isImmuneToFire = true
        experienceValue = 10
    }

    override fun initEntityAI() {
        tasks.addTask(4, AIFireballAttack(this))
        tasks.addTask(5, EntityAIMoveTowardsRestriction(this, 1.0))
        tasks.addTask(7, EntityAIWanderAvoidWater(this, 1.0, 0f))
        tasks.addTask(8, EntityAIWatchClosest(this, EntityPlayer::class.java, 8f))
        tasks.addTask(8, EntityAILookIdle(this))
        targetTasks.addTask(1, EntityAIHurtByTarget(this, true))
        targetTasks.addTask(2, EntityAINearestAttackableTarget(this, EntityPlayer::class.java, true))
    }

    override fun applyEntityAttributes() {
        super.applyEntityAttributes()
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).baseValue = 6.0
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).baseValue = 0.23000000417232513
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).baseValue = 48.0
    }

    override fun entityInit() {
        super.entityInit()
        dataManager.register(ON_FIRE, 0.toByte())
    }

    override fun getAmbientSound(): SoundEvent = SoundEvents.ENTITY_BLAZE_AMBIENT

    override fun getHurtSound(damageSourceIn: DamageSource): SoundEvent = SoundEvents.ENTITY_BLAZE_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.ENTITY_BLAZE_DEATH

    @SideOnly(Side.CLIENT)
    override fun getBrightnessForRender() = 15728880

    override fun getBrightness() = 1f

    override fun onLivingUpdate() {
        if (!onGround && motionY < 0.0) motionY *= 0.6

        if (world.isRemote) {
            if (rand.nextInt(24) == 0 && !isSilent) {
                world.playSound(
                    posX + 0.5, posY + 0.5, posZ + 0.5, SoundEvents.ENTITY_BLAZE_BURN, soundCategory,
                    1f + rand.nextFloat(), rand.nextFloat() * 0.7f + 0.3f, false,
                )
            }
            world.spawnParticle(
                EnumParticleTypes.END_ROD,
                posX + (rand.nextDouble() - 0.5) * width, posY + rand.nextDouble() * height, posZ + (rand.nextDouble() - 0.5) * width,
                0.0, 0.0, 0.0,
            )
        }

        super.onLivingUpdate()
    }

    override fun updateAITasks() {
        if (isWet) attackEntityFrom(DamageSource.DROWN, 1f)

        if (--heightOffsetUpdateTime <= 0) {
            heightOffsetUpdateTime = 100
            heightOffset = 0.5f + rand.nextGaussian().toFloat() * 3f
        }

        val target = attackTarget
        if (target != null && target.posY + target.eyeHeight > posY + eyeHeight + heightOffset) {
            motionY += (0.30000001192092896 - motionY) * 0.30000001192092896
            isAirBorne = true
        }

        super.updateAITasks()
    }

    override fun fall(distance: Float, damageMultiplier: Float) {}

    override fun isBurning() = isCharged

    override fun getLootTable(): ResourceLocation = LootHandler.END_GUARD

    val isCharged: Boolean get() = dataManager.get(ON_FIRE).toInt() and 1 != 0

    fun setOnFire(onFire: Boolean) {
        val flags = dataManager.get(ON_FIRE).toInt()
        dataManager.set(ON_FIRE, (if (onFire) flags or 1 else flags and -2).toByte())
    }

    override fun isValidLightLevel() = true

    private class AIFireballAttack(private val blaze: EntityEndGuard) : EntityAIBase() {
        private var attackStep = 0
        private var attackTime = 0

        init {
            mutexBits = 6
        }

        override fun shouldExecute(): Boolean = blaze.attackTarget?.isEntityAlive == true

        override fun startExecuting() {
            attackStep = 0
        }

        override fun resetTask() = blaze.setOnFire(false)

        override fun updateTask() {
            --attackTime
            val target = blaze.attackTarget!!
            val distSq = blaze.getDistanceSq(target)

            if (distSq < 4.0) {
                if (attackTime <= 0) {
                    attackTime = 20
                    blaze.attackEntityAsMob(target)
                }
                blaze.moveHelper.setMoveTo(target.posX, target.posY, target.posZ, 1.0)
            } else if (distSq < followDistance * followDistance) {
                val dx = target.posX - blaze.posX
                val dy = target.entityBoundingBox.minY + target.height / 2.0 - (blaze.posY + blaze.height / 2.0)
                val dz = target.posZ - blaze.posZ

                if (attackTime <= 0) {
                    ++attackStep

                    if (attackStep == 1) {
                        attackTime = 60
                        blaze.setOnFire(false)
                    } else if (attackStep <= 4) {
                        attackTime = 8
                    } else {
                        attackTime = 100
                        attackStep = 0
                        blaze.setOnFire(false)
                    }

                    if (attackStep > 1) {
                        val f = MathHelper.sqrt(MathHelper.sqrt(distSq)) * 0.5f
                        blaze.world.playEvent(null, 1018, BlockPos(blaze.posX.toInt(), blaze.posY.toInt(), blaze.posZ.toInt()), 0)

                        val fireball = EntityEntropyFireball(
                            blaze.world, blaze, dx + blaze.rng.nextGaussian() * f, dy, dz + blaze.rng.nextGaussian() * f,
                        )
                        fireball.posY = blaze.posY + blaze.height / 2.0 + 0.5
                        blaze.world.spawnEntity(fireball)
                    }
                }

                blaze.lookHelper.setLookPositionWithEntity(target, 10f, 10f)
            } else {
                blaze.navigator.clearPath()
                blaze.moveHelper.setMoveTo(target.posX, target.posY, target.posZ, 1.0)
            }

            super.updateTask()
        }

        private val followDistance: Double
            get() = blaze.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)?.attributeValue ?: 16.0
    }
}
