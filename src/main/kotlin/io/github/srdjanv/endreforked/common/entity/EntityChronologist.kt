package io.github.srdjanv.endreforked.common.entity

import io.github.srdjanv.endreforked.common.ModPotions
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.*
import net.minecraft.entity.monster.EntityEndermite
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.minecraft.world.storage.loot.LootTableList
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

class EntityChronologist(world: World) : EndEntity(world) {
    override fun initEntityAI() {
        tasks.addTask(1, EntityAIAttackMelee(this, 1.0, false))
        tasks.addTask(2, EntityAIWanderAvoidWater(this, 1.0, 0f))
        tasks.addTask(3, EntityAIWatchClosest(this, EntityPlayer::class.java, 8f))
        tasks.addTask(4, EntityAILookIdle(this))
        targetTasks.addTask(1, AIFindLookingPlayer(this))
        targetTasks.addTask(2, EntityAIHurtByTarget(this, false))
        targetTasks.addTask(3, EntityAINearestAttackableTarget(this, EntityEndermite::class.java, true))
    }

    override fun applyEntityAttributes() {
        super.applyEntityAttributes()
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).baseValue = 40.0
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).baseValue = 0.14
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).baseValue = 4.0
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).baseValue = 8.0
    }

    override fun getEyeHeight() = 2.55f

    override fun getLootTable(): ResourceLocation = LootTableList.ENTITIES_ENDERMAN

    override fun getAmbientSound(): SoundEvent =
        if (isScreaming) SoundEvents.ENTITY_ENDERMEN_SCREAM else SoundEvents.ENTITY_ENDERMEN_AMBIENT

    override fun getHurtSound(damageSourceIn: DamageSource): SoundEvent = SoundEvents.ENTITY_ENDERMEN_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.ENTITY_ENDERMEN_DEATH

    override fun shouldAttackPlayer(player: EntityPlayer) =
        !player.isPotionActive(ModPotions.ENDER_EYES) && super.shouldAttackPlayer(player)

    @SideOnly(Side.CLIENT)
    override fun getBrightnessForRender(): Int {
        val pos = BlockPos.MutableBlockPos(MathHelper.floor(posX), 0, MathHelper.floor(posZ))
        if (world != null && world.isBlockLoaded(pos)) {
            pos.y = MathHelper.floor(posY + eyeHeight)
            return world.getCombinedLight(pos, 0)
        }
        return 15
    }
}
