package io.github.srdjanv.endreforked.common.entity

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.EntityAINearestAttackableTarget
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.item.Item
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.DataSerializers
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.pathfinding.PathNodeType
import net.minecraft.util.DamageSource
import net.minecraft.util.EntityDamageSourceIndirect
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import java.util.UUID

abstract class EndEntity(world: World) : EntityMob(world) {
    companion object {
        private val ATTACKING_SPEED_BOOST = AttributeModifier(
            UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0"), "Attacking speed boost", 0.15000000596046448, 0,
        ).setSaved(false)
        private val SCREAMING: DataParameter<Boolean> = EntityDataManager.createKey(EndEntity::class.java, DataSerializers.BOOLEAN)
    }

    private var lifetime = 0
    private var lastCreepySound = 0
    protected var targetChangeTime = 0

    init {
        setSize(0.6f, 2.9f)
        stepHeight = 1f
        setPathPriority(PathNodeType.WATER, -1f)
    }

    val isScreaming: Boolean get() = dataManager.get(SCREAMING)

    override fun entityInit() {
        super.entityInit()
        dataManager.register(SCREAMING, false)
    }

    override fun onLivingUpdate() {
        if (world.isRemote) {
            repeat(2) {
                world.spawnParticle(
                    EnumParticleTypes.PORTAL,
                    posX + (rand.nextDouble() - 0.5) * width,
                    posY + rand.nextDouble() * height - 0.25,
                    posZ + (rand.nextDouble() - 0.5) * width,
                    (rand.nextDouble() - 0.5) * 2.0, -rand.nextDouble(), (rand.nextDouble() - 0.5) * 2.0,
                )
            }
        }

        isJumping = false
        super.onLivingUpdate()

        if (!world.isRemote) {
            if (!isNoDespawnRequired) ++lifetime
            if (lifetime >= 1200) setDead()
        }
    }

    override fun updateAITasks() {
        if (isWet) attackEntityFrom(DamageSource.DROWN, 1f)

        if (world.isDaytime && ticksExisted >= targetChangeTime + 600) {
            val light = brightness
            if (light > 0.5f && world.canSeeSky(BlockPos(this)) && rand.nextFloat() * 30f < (light - 0.4f) * 2f) {
                attackTarget = null
                teleportRandomly()
            }
        }

        super.updateAITasks()
    }

    override fun setAttackTarget(entityTarget: EntityLivingBase?) {
        super.setAttackTarget(entityTarget)
        val speed = getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)

        if (entityTarget == null) {
            targetChangeTime = 0
            dataManager.set(SCREAMING, false)
            speed.removeModifier(ATTACKING_SPEED_BOOST)
        } else {
            targetChangeTime = ticksExisted
            dataManager.set(SCREAMING, true)
            if (!speed.hasModifier(ATTACKING_SPEED_BOOST)) speed.applyModifier(ATTACKING_SPEED_BOOST)
        }
    }

    override fun notifyDataManagerChange(key: DataParameter<*>) {
        if (SCREAMING == key && isScreaming && world.isRemote) playEndermanSound()
        super.notifyDataManagerChange(key)
    }

    override fun writeEntityToNBT(compound: NBTTagCompound) {
        super.writeEntityToNBT(compound)
        compound.setInteger("Lifetime", lifetime)
    }

    override fun readEntityFromNBT(compound: NBTTagCompound) {
        super.readEntityFromNBT(compound)
        lifetime = compound.getInteger("Lifetime")
    }

    fun teleportRandomly(): Boolean =
        teleportTo(posX + (rand.nextDouble() - 0.5) * 64.0, posY + (rand.nextInt(64) - 32), posZ + (rand.nextDouble() - 0.5) * 64.0)

    private fun teleportToEntity(entity: Entity): Boolean {
        val vec = Vec3d(
            posX - entity.posX, entityBoundingBox.minY + height / 2.0 - entity.posY + entity.getEyeHeight(), posZ - entity.posZ,
        ).normalize()
        return teleportTo(
            posX + (rand.nextDouble() - 0.5) * 8.0 - vec.x * 16.0,
            posY + (rand.nextInt(16) - 8) - vec.y * 16.0,
            posZ + (rand.nextDouble() - 0.5) * 8.0 - vec.z * 16.0,
        )
    }

    fun teleportTo(x: Double, y: Double, z: Double): Boolean {
        val event = EnderTeleportEvent(this, x, y, z, 0f)
        if (MinecraftForge.EVENT_BUS.post(event)) return false
        val teleported = attemptTeleport(event.targetX, event.targetY, event.targetZ)

        if (teleported) {
            world.playSound(null, prevPosX, prevPosY, prevPosZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, soundCategory, 1f, 1f)
            playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1f, 1f)
        }
        return teleported
    }

    open fun shouldAttackPlayer(player: EntityPlayer): Boolean {
        if (player.inventory.armorInventory[3].item === Item.getItemFromBlock(Blocks.PUMPKIN)) return false
        val look = player.getLook(1f).normalize()
        var toMe = Vec3d(
            posX - player.posX, entityBoundingBox.minY + eyeHeight - (player.posY + player.getEyeHeight()), posZ - player.posZ,
        )
        val distance = toMe.length()
        toMe = toMe.normalize()
        return look.dotProduct(toMe) > 1.0 - 0.025 / distance && player.canEntityBeSeen(this)
    }

    override fun attackEntityFrom(source: DamageSource, amount: Float): Boolean {
        if (isEntityInvulnerable(source)) return false
        if (source is EntityDamageSourceIndirect) return (0 until 64).any { teleportRandomly() }

        val hurt = super.attackEntityFrom(source, amount)
        if (source.isUnblockable && rand.nextInt(10) != 0) teleportRandomly()
        return hurt
    }

    private fun playEndermanSound() {
        if (ticksExisted >= lastCreepySound + 400) {
            lastCreepySound = ticksExisted
            if (!isSilent) {
                world.playSound(posX, posY + eyeHeight, posZ, SoundEvents.ENTITY_ENDERMEN_STARE, soundCategory, 2.5f, 1f, false)
            }
        }
    }

    override fun getCanSpawnHere() = super.getCanSpawnHere() && world.canSeeSky(BlockPos(this))

    protected class AIFindLookingPlayer(private val entity: EndEntity) : EntityAINearestAttackableTarget<EntityPlayer>(entity, EntityPlayer::class.java, false) {
        private var player: EntityPlayer? = null
        private var aggroTime = 0
        private var teleportTime = 0

        override fun shouldExecute(): Boolean {
            val distance = targetDistance
            player = entity.world.getNearestAttackablePlayer(
                entity.posX, entity.posY, entity.posZ, distance, distance, null,
            ) { it != null && entity.shouldAttackPlayer(it) }
            return player != null
        }

        override fun startExecuting() {
            aggroTime = 5
            teleportTime = 0
        }

        override fun resetTask() {
            player = null
            super.resetTask()
        }

        override fun shouldContinueExecuting(): Boolean {
            val looking = player
            if (looking != null) {
                if (!entity.shouldAttackPlayer(looking)) return false
                entity.faceEntity(looking, 10f, 10f)
                return true
            }
            return targetEntity?.isEntityAlive == true || super.shouldContinueExecuting()
        }

        override fun updateTask() {
            val looking = player
            if (looking != null) {
                if (--aggroTime <= 0) {
                    targetEntity = looking
                    player = null
                    super.startExecuting()
                }
                return
            }
            val target = targetEntity
            if (target != null) {
                if (entity.shouldAttackPlayer(target)) {
                    if (target.getDistanceSq(entity) < 16.0) entity.teleportRandomly()
                    teleportTime = 0
                } else if (target.getDistanceSq(entity) > 256.0 && teleportTime++ >= 30 && entity.teleportToEntity(target)) {
                    teleportTime = 0
                }
            }
            super.updateTask()
        }
    }
}
