package io.github.srdjanv.endreforked.common.entity

import io.github.srdjanv.endreforked.common.LootHandler
import io.github.srdjanv.endreforked.common.configs.Configs
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityCreature
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.*
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.init.SoundEvents
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.potion.PotionEffect
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class EntityWatcher @JvmOverloads constructor(
    world: World,
    val maxEscapes: Int = Configs.SERVER_SIDE_CONFIGS.ENTITY_CONFIGS.WATCHER.max_escapes,
    private val huntCooldown: Int = Configs.SERVER_SIDE_CONFIGS.ENTITY_CONFIGS.WATCHER.hunt_cooldown,
) : EndEntity(world) {
    companion object {
        private val TIMES_ESCAPED: DataParameter<Int> = EntityDataManager.createKey(EntityWatcher::class.java, DataSerializers.VARINT)
        private val HUNT_PLAYER: DataParameter<Boolean> = EntityDataManager.createKey(EntityWatcher::class.java, DataSerializers.BOOLEAN)
    }

    init {
        dataManager.register(TIMES_ESCAPED, 0)
        dataManager.register(HUNT_PLAYER, false)
    }

    override fun initEntityAI() {
        tasks.addTask(0, EntityAIAttackMelee(this, 1.0, false))
        tasks.addTask(1, EntityAIKeepDistanceAndWatch(this))
        tasks.addTask(3, EntityAIWanderAvoidWater(this, 1.0, 0f))
        targetTasks.addTask(1, EntityAIHurtByTargetReinforce(this))
        targetTasks.addTask(2, EntityAINearestAttackableTarget(this, EntityPlayer::class.java, 10, true, false) { dataManager.get(HUNT_PLAYER) })
    }

    override fun applyEntityAttributes() {
        super.applyEntityAttributes()
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).baseValue = 20.0
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).baseValue = 0.34
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).baseValue = 7.0
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).baseValue = 64.0
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).baseValue = 0.0
    }

    // Original behaviour kept: this does not call super, so the day teleport of EndEntity never runs.
    override fun updateAITasks() {
        if (ticksExisted >= targetChangeTime + huntCooldown) {
            dataManager.set(TIMES_ESCAPED, 0)
            dataManager.set(HUNT_PLAYER, false)
        }

        if (isWet) attackEntityFrom(DamageSource.DROWN, 1f)
        if (!dataManager.get(HUNT_PLAYER) && world.isDaytime && ticksExisted >= targetChangeTime + 60 * 20) {
            val light = brightness
            if (light > 0.5f && world.canSeeSky(BlockPos(this)) && rand.nextFloat() * 30f < (light - 0.4f) * 2f) {
                attackTarget = null
                teleportRandomly()
            }
        }
    }

    override fun getLootTable(): ResourceLocation = LootHandler.WATCHER

    override fun attackEntityAsMob(entityIn: Entity): Boolean {
        val hit = super.attackEntityAsMob(entityIn)
        if (hit && heldItemMainhand.isEmpty && entityIn is EntityLivingBase) {
            val difficulty = world.getDifficultyForLocation(BlockPos(this)).additionalDifficulty
            entityIn.addPotionEffect(PotionEffect(MobEffects.BLINDNESS, 140 * difficulty.toInt()))
        }
        return hit
    }

    override fun getEyeHeight() = 2.55f

    override fun getAmbientSound(): SoundEvent =
        if (isScreaming) SoundEvents.ENTITY_ENDERMEN_SCREAM else SoundEvents.ENTITY_ENDERMEN_AMBIENT

    override fun getHurtSound(damageSourceIn: DamageSource): SoundEvent = SoundEvents.ENTITY_ENDERMEN_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.ENTITY_ENDERMEN_DEATH

    private class EntityAIHurtByTargetReinforce(creature: EntityCreature) : EntityAIHurtByTarget(creature, true) {
        override fun alertOthers() {
            val revenge = taskOwner.revengeTarget ?: return // should not be possible
            val range = targetDistance
            val area = AxisAlignedBB(taskOwner.posX, taskOwner.posY, taskOwner.posZ, taskOwner.posX + 1.0, taskOwner.posY + 1.0, taskOwner.posZ + 1.0)
                .grow(range, 10.0, range)
            val mobs = taskOwner.world.getEntitiesWithinAABB(EntityEnderman::class.java, area) +
                taskOwner.world.getEntitiesWithinAABB(EntityWatcher::class.java, area)

            for (mob in mobs) {
                if (mob is EntityWatcher) mob.dataManager.set(HUNT_PLAYER, true)
                if (taskOwner !== mob && mob.attackTarget == null) setEntityAttackTarget(mob, revenge)
            }
        }
    }

    private class EntityAIKeepDistanceAndWatch(private val watcher: EntityWatcher) : EntityAIBase() {
        private var followingPlayer: EntityPlayer? = null
        private val navigation = watcher.navigator
        private val minPlayerDis = 8f
        private val buffPlayerDis = 4f
        private val maxFailedEscapes = 3
        private val maxTPPosSearch = 32
        private var failedEscapes = 0
        private var timeToRecalcPath = 0
        private var follow = true

        private val followRange: Double
            get() = watcher.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)?.attributeValue ?: 16.0

        override fun shouldExecute(): Boolean {
            if (watcher.dataManager.get(TIMES_ESCAPED) > watcher.maxEscapes || watcher.dataManager.get(HUNT_PLAYER)) {
                watcher.dataManager.set(HUNT_PLAYER, true)
                return false
            }
            val range = followRange
            followingPlayer = watcher.world.getNearestAttackablePlayer(watcher.posX, watcher.posY, watcher.posZ, range, range, null, null)
            return followingPlayer != null
        }

        override fun shouldContinueExecuting(): Boolean {
            val player = followingPlayer ?: return false
            if (watcher.attackingEntity != null) return false
            if (watcher.dataManager.get(HUNT_PLAYER)) return false
            if (watcher.dataManager.get(TIMES_ESCAPED) > watcher.maxEscapes) return false

            val distance = watcher.getDistance(player)
            val range = followRange
            if (distance > range) return false
            if (distance > minPlayerDis + buffPlayerDis) {
                follow = true
                return true
            }
            if (distance > minPlayerDis) {
                follow = false
                return true
            }

            // try to escape player
            val newPos = BlockPos.MutableBlockPos()
            for (i in 0 until maxTPPosSearch) {
                val x = player.posX + (watcher.rand.nextDouble() - 0.5) * range
                val y = player.posY + (watcher.rand.nextDouble() - 0.5) * range
                val z = player.posZ + (watcher.rand.nextDouble() - 0.5) * range
                newPos.setPos(x, y, z)

                val newDistance = player.position.getDistance(newPos.x, newPos.y, newPos.z)
                if (newDistance > range || newDistance < minPlayerDis) continue

                // Probe the path from the candidate position, then put the position back.
                val orgX = watcher.posX
                val orgY = watcher.posY
                val orgZ = watcher.posZ
                val orgPath = navigation.path
                watcher.posX = x
                watcher.posY = y
                watcher.posZ = z
                navigation.clearPath()
                val path = navigation.getPathToPos(player.position)
                watcher.posX = orgX
                watcher.posY = orgY
                watcher.posZ = orgZ
                if (path == null) {
                    navigation.setPath(orgPath, 1.0)
                    continue
                }
                val start = path.getPathPointFromIndex(0)
                if (!watcher.teleportTo(start.x.toDouble(), start.y.toDouble(), start.z.toDouble())) continue

                follow = true
                watcher.dataManager.set(TIMES_ESCAPED, watcher.dataManager.get(TIMES_ESCAPED) + 1)
                return true
            }
            if (failedEscapes > maxFailedEscapes) {
                watcher.dataManager.set(TIMES_ESCAPED, 0)
                watcher.dataManager.set(HUNT_PLAYER, true)
                failedEscapes = 0
            } else failedEscapes++
            return false
        }

        override fun startExecuting() {
            timeToRecalcPath = 0
        }

        override fun resetTask() {
            followingPlayer = null
            navigation.clearPath()
        }

        override fun updateTask() {
            val player = followingPlayer
            if (player == null || watcher.leashed) return
            watcher.lookHelper.setLookPositionWithEntity(player, 10f, watcher.verticalFaceSpeed.toFloat())

            if (!follow) {
                navigation.clearPath()
                return
            }
            if (--timeToRecalcPath <= 0) {
                timeToRecalcPath = 10
                val distance = watcher.getDistance(player)

                if (distance > minPlayerDis + buffPlayerDis) navigation.tryMoveToEntityLiving(player, 1.2)
                else if (distance > minPlayerDis) navigation.clearPath()
            }
        }
    }
}
