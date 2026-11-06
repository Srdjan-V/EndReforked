package io.github.srdjanv.endreforked.common.entity

import io.github.srdjanv.endreforked.common.LootHandler
import io.github.srdjanv.endreforked.utils.MessageHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.*
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityEndermite
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.entity.projectile.EntityWitherSkull
import net.minecraft.init.MobEffects
import net.minecraft.init.SoundEvents
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.potion.PotionEffect
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.BossInfo
import net.minecraft.world.BossInfoServer
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class EntityLord(world: World) : EntityMob(world) {
    // The ids of the data parameters follow the order of these three keys.
    companion object {
        private val ARMS_RAISED: DataParameter<Boolean> = EntityDataManager.createKey(EntityLord::class.java, DataSerializers.BOOLEAN)
        private val ON_FIRE: DataParameter<Byte> = EntityDataManager.createKey(EntityLord::class.java, DataSerializers.BYTE)
        private val TARGET_ENTITY: DataParameter<Int> = EntityDataManager.createKey(EntityLord::class.java, DataSerializers.VARINT)
    }

    private val bossInfo = BossInfoServer(displayName, BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS).apply { setDarkenSky(true) }
    private var heightOffset = 0.5f
    private var heightOffsetUpdateTime = 0

    init {
        isImmuneToFire = true
        experienceValue = 100
    }

    override fun initEntityAI() {
        tasks.addTask(4, AIFireballAttack(this))
        tasks.addTask(5, EntityAIMoveTowardsRestriction(this, 1.0))
        tasks.addTask(8, EntityAIWatchClosest(this, EntityPlayer::class.java, 16f))
        tasks.addTask(8, EntityAILookIdle(this))
        targetTasks.addTask(1, EntityAIHurtByTarget(this, true))
        targetTasks.addTask(2, EntityAINearestAttackableTarget(this, EntityPlayer::class.java, true))
        tasks.addTask(2, EntityAIMoveTowardsTarget(this, 0.9, 8f))
    }

    override fun updateAITasks() {
        if (--heightOffsetUpdateTime <= 0) {
            heightOffsetUpdateTime = 100
            heightOffset = 0.5f + rand.nextGaussian().toFloat() * 3f
        }

        val target = attackTarget
        if (target != null && target.posY + target.eyeHeight > posY + eyeHeight + heightOffset) {
            motionY += (0.3 - motionY) * 0.3
            isAirBorne = true
        }

        if (ticksExisted % 20 == 0 && health < 40) {
            addPotionEffect(PotionEffect(MobEffects.HASTE, 100, 1))
            addPotionEffect(PotionEffect(MobEffects.SPEED, 100, 1))
            heal(2f)
        }

        if (world.isRemote) return
        if (ticksExisted % 400 == 0) {
            val hp = health
            if (hp < 150 && hp > 120) MessageHelper.customGroup(world, "Lord: You... Are you still fighting?")
            if (hp < 120 && hp > 90) MessageHelper.customGroup(world, "Lord: Are you trying to kill everyone?")
            if (hp < 90 && hp > 50) MessageHelper.customGroup(world, "Lord: You know nothing!")
            if (hp < 30 && hp > 10) MessageHelper.customGroup(world, "Lord: To rise you need to fall!")
        }

        for (entity in world.getEntitiesWithinAABBExcludingEntity(this, entityBoundingBox.grow(64.0, 64.0, 64.0))) {
            if (entity is EntityWither) {
                world.removeEntity(entity)
                MessageHelper.customGroup(world, "Lord: You can not compare the creation and creator!")
            } else if (entity is EntityLord) {
                world.removeEntity(entity)
                MessageHelper.customGroup(world, "Lord: This is not fair now!")
            }
        }
        bossInfo.percent = health / maxHealth
    }

    override fun entityInit() {
        super.entityInit()
        dataManager.register(ON_FIRE, 0.toByte())
        dataManager.register(TARGET_ENTITY, 0)
        dataManager.register(ARMS_RAISED, false)
    }

    override fun applyEntityAttributes() {
        super.applyEntityAttributes()
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).baseValue = 200.0
        getEntityAttribute(SharedMonsterAttributes.ARMOR).baseValue = 12.0
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).baseValue = 0.0
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).baseValue = 6.0
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).baseValue = 0.34
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).baseValue = 48.0
    }

    @get:SideOnly(Side.CLIENT)
    val isArmsRaised: Boolean get() = dataManager.get(ARMS_RAISED)

    override fun isImmuneToExplosions() = true

    override fun getHurtSound(damageSourceIn: DamageSource): SoundEvent = SoundEvents.ENTITY_BLAZE_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.ENTITY_BLAZE_DEATH

    override fun getAmbientSound(): SoundEvent = SoundEvents.ENTITY_ENDERMEN_AMBIENT

    override fun fall(distance: Float, damageMultiplier: Float) {}

    override fun isBurning() = dataManager.get(ON_FIRE).toInt() and 1 != 0

    fun setOnFire(onFire: Boolean) {
        val flags = dataManager.get(ON_FIRE).toInt()
        dataManager.set(ON_FIRE, (if (onFire) flags or 1 else flags and -2).toByte())
    }

    override fun addTrackingPlayer(player: EntityPlayerMP) {
        super.addTrackingPlayer(player)
        bossInfo.addPlayer(player)
    }

    override fun readEntityFromNBT(compound: NBTTagCompound) {
        super.readEntityFromNBT(compound)
        if (hasCustomName()) bossInfo.name = displayName
    }

    override fun isNonBoss() = false

    override fun setCustomNameTag(name: String) {
        super.setCustomNameTag(name)
        bossInfo.name = displayName
    }

    override fun removeTrackingPlayer(player: EntityPlayerMP) {
        super.removeTrackingPlayer(player)
        bossInfo.removePlayer(player)
    }

    override fun onLivingUpdate() {
        if (!onGround && motionY < 0.0) motionY *= 0.4
        dataManager.set(ARMS_RAISED, !onGround)

        if (world.isRemote && rand.nextInt(24) == 0 && !isSilent) {
            world.playSound(
                posX + 0.5, posY + 0.5, posZ + 0.5, SoundEvents.ENTITY_ENDERMEN_HURT, soundCategory,
                1f + rand.nextFloat(), rand.nextFloat() * 0.7f + 0.3f, false,
            )
        }

        super.onLivingUpdate()
    }

    override fun onDeathUpdate() {
        setDead()
        MessageHelper.customGroup(world, "Lord: Ha-ha-ha...")
        if (!world.isRemote) {
            val ender = EntityEnderman(world)
            ender.setLocationAndAngles(posX, posY + 1, posZ, rotationYaw, rotationPitch)
            world.spawnEntity(ender)
        }
    }

    override fun getLootTable(): ResourceLocation = LootHandler.LORD

    // Original bug kept: the spawned endermite stays at the world origin, it is never moved to the lord.
    override fun attackEntityAsMob(entityIn: Entity): Boolean {
        val hit = super.attackEntityAsMob(entityIn)
        if (hit && heldItemMainhand.isEmpty && entityIn is EntityLivingBase) {
            world.spawnEntity(EntityEndermite(world))
            val difficulty = world.getDifficultyForLocation(BlockPos(this)).additionalDifficulty
            entityIn.addPotionEffect(PotionEffect(MobEffects.LEVITATION, 140 * difficulty.toInt()))
        }
        return hit
    }

    private class AIFireballAttack(private val lord: EntityLord) : EntityAIBase() {
        private var attackStep = 0
        private var attackTime = 0

        init {
            mutexBits = 6
        }

        override fun shouldExecute(): Boolean = lord.attackTarget?.isEntityAlive == true

        override fun startExecuting() {
            attackStep = 0
        }

        override fun resetTask() = lord.setOnFire(false)

        override fun updateTask() {
            --attackTime
            val target = lord.attackTarget!!
            val distSq = lord.getDistanceSq(target)

            if (distSq < 4.0) {
                if (attackTime <= 0) {
                    attackTime = 20
                    lord.attackEntityAsMob(target)
                }
                lord.moveHelper.setMoveTo(target.posX, target.posY, target.posZ, 1.0)
            } else if (distSq < followDistance * followDistance) {
                val dx = target.posX - lord.posX
                val dy = target.entityBoundingBox.minY + target.height / 2.0 - (lord.posY + lord.height / 2.0)
                val dz = target.posZ - lord.posZ

                if (attackTime <= 0) {
                    ++attackStep

                    if (attackStep == 1) {
                        attackTime = 20
                        lord.setOnFire(false)
                    } else if (attackStep <= 4) {
                        attackTime = 8
                    } else {
                        attackTime = 20
                        attackStep = 0
                        lord.setOnFire(false)
                    }

                    if (attackStep > 1) {
                        val f = MathHelper.sqrt(MathHelper.sqrt(distSq)) * 0.5f
                        lord.world.playEvent(null, 1018, BlockPos(lord.posX.toInt(), lord.posY.toInt(), lord.posZ.toInt()), 0)

                        val skull = EntityWitherSkull(
                            lord.world, lord, dx + lord.rng.nextGaussian() * f, dy, dz + lord.rng.nextGaussian() * f,
                        )
                        skull.posY = lord.posY + lord.height / 2.0 + 0.5
                        lord.world.spawnEntity(skull)
                    }
                }

                lord.lookHelper.setLookPositionWithEntity(target, 10f, 10f)
            } else {
                lord.navigator.clearPath()
                lord.moveHelper.setMoveTo(target.posX, target.posY, target.posZ, 1.0)
            }

            super.updateTask()
        }

        private val followDistance: Double
            get() = lord.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)?.attributeValue ?: 64.0
    }
}
