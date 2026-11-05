package io.github.srdjanv.endreforked.common.entity;

import java.util.Objects;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import io.github.srdjanv.endreforked.common.LootHandler;
import io.github.srdjanv.endreforked.utils.MessageHelper;

public class EntityLord extends EntityMob {

    private final BossInfoServer bossInfo = (BossInfoServer) (new BossInfoServer(this.getDisplayName(),
            BossInfo.Color.PURPLE,
            BossInfo.Overlay.PROGRESS)).setDarkenSky(true);

    private float heightOffset = 0.5F;
    private int heightOffsetUpdateTime;
    private static final DataParameter<Boolean> ARMS_RAISED = EntityDataManager.createKey(EntityLord.class,
            DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> ON_FIRE = EntityDataManager.createKey(EntityLord.class,
            DataSerializers.BYTE);
    private static final DataParameter<Integer> TARGET_ENTITY = EntityDataManager.createKey(EntityLord.class,
            DataSerializers.VARINT);

    public EntityLord(World worldIn) {
        super(worldIn);
        isImmuneToFire = true;
        experienceValue = 100;
    }

    @Override
    public void initEntityAI() {
        this.tasks.addTask(4, new EntityLord.AIFireballAttack(this));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[0]));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.tasks.addTask(2, new EntityAIMoveTowardsTarget(this, 0.9D, 8.0F));
    }

    @Override
    protected void updateAITasks() {
        if (--heightOffsetUpdateTime <= 0) {
            heightOffsetUpdateTime = 100;
            heightOffset = 0.5F + (float) rand.nextGaussian() * 3.0F;
        }

        EntityLivingBase attackTarget = getAttackTarget();
        if (attackTarget != null && attackTarget.posY + (double) attackTarget.getEyeHeight() >
                posY + (double) getEyeHeight() + (double) heightOffset) {
            motionY += (0.3D - motionY) * 0.3D;
            isAirBorne = true;
        }

        if (ticksExisted % 20 == 0 && getHealth() < 40) {
            addPotionEffect(new PotionEffect(MobEffects.HASTE, 100, 1));
            addPotionEffect(new PotionEffect(MobEffects.SPEED, 100, 1));
            heal(2.0F);
        }

        if (world.isRemote) return;
        if (ticksExisted % 400 == 0) {
            if (getHealth() < 150 && getHealth() > 120)
                MessageHelper.LordGroup(world, "Lord: You... Are you still fighting?");
            if (this.getHealth() < 120 && this.getHealth() > 90)
                MessageHelper.LordGroup(world, "Lord: Are you trying to kill everyone?");
            if (this.getHealth() < 90 && this.getHealth() > 50)
                MessageHelper.LordGroup(world, "Lord: You know nothing!");
            if (this.getHealth() < 30 && this.getHealth() > 10)
                MessageHelper.LordGroup(world, "Lord: To rise you need to fall!");
        }

        for (Entity entity : world.getEntitiesWithinAABBExcludingEntity(this,
                getEntityBoundingBox().grow(64.0D, 64.0D, 64.0D))) {
            if (entity instanceof EntityWither) {
                world.removeEntity(entity);
                MessageHelper.LordGroup(world, "Lord: You can not compare the creation and creator!");
            } else if (entity instanceof EntityLord) {
                world.removeEntity(entity);
                MessageHelper.LordGroup(world, "Lord: This is not fair now!");
            }
        }
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(ON_FIRE, (byte) 0);
        this.getDataManager().register(TARGET_ENTITY, 0);
        this.getDataManager().register(ARMS_RAISED, Boolean.FALSE);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(12.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.34D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48.0D);
    }

    public void setArmsRaised(boolean armsRaised) {
        this.getDataManager().set(ARMS_RAISED, armsRaised);
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean isArmsRaised() {
        return this.getDataManager().get(ARMS_RAISED);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BLAZE_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ENDERMEN_AMBIENT;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {}

    @Override
    public boolean isBurning() {
        return (this.dataManager.get(ON_FIRE) & 1) != 0;
    }

    public void setOnFire(boolean onFire) {
        byte b0 = this.dataManager.get(ON_FIRE);

        if (onFire) {
            b0 = (byte) (b0 | 1);
        } else b0 = (byte) (b0 & -2);
        this.dataManager.set(ON_FIRE, b0);
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);

        if (this.hasCustomName()) {
            this.bossInfo.setName(this.getDisplayName());
        }
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        bossInfo.setName(this.getDisplayName());
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        bossInfo.removePlayer(player);
    }

    @Override
    public void onLivingUpdate() {
        if (!onGround && motionY < 0.0D) motionY *= 0.4D;
        setArmsRaised(!onGround);

        if (world.isRemote) {
            if (rand.nextInt(24) == 0 && !isSilent()) {
                world.playSound(posX + 0.5D, posY + 0.5D, posZ + 0.5D,
                        SoundEvents.ENTITY_ENDERMEN_HURT, getSoundCategory(), 1.0F + rand.nextFloat(),
                        rand.nextFloat() * 0.7F + 0.3F, false);
            }
        }

        super.onLivingUpdate();
    }

    @Override
    protected void onDeathUpdate() {
        setDead();
        MessageHelper.LordGroup(world, "Lord: Ha-ha-ha...");
        if (!world.isRemote) {
            EntityEnderman ender = new EntityEnderman(world);
            ender.setLocationAndAngles(posX, posY + 1, posZ, rotationYaw, rotationPitch);
            world.spawnEntity(ender);
        }
    }

    @Override
    protected ResourceLocation getLootTable() {
        return LootHandler.LORD;
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);
        EntityEndermite entityEndermite = new EntityEndermite(world);
        if (flag && getHeldItemMainhand().isEmpty() && entityIn instanceof EntityLivingBase) {
            world.spawnEntity(entityEndermite);
            float f = world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();
            ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 140 * (int) f));
        }

        return flag;
    }

    static class AIFireballAttack extends EntityAIBase {

        private final EntityLord entityLord;
        private int attackStep;
        private int attackTime;

        public AIFireballAttack(EntityLord entityLord) {
            this.entityLord = entityLord;
            this.setMutexBits(6);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase entitylivingbase = this.entityLord.getAttackTarget();
            return entitylivingbase != null && entitylivingbase.isEntityAlive();
        }

        @Override
        public void startExecuting() {
            attackStep = 0;
        }

        @Override
        public void resetTask() {
            entityLord.setOnFire(false);
        }

        @Override
        public void updateTask() {
            --this.attackTime;
            EntityLivingBase entitylivingbase = entityLord.getAttackTarget();
            double d0 = this.entityLord.getDistanceSq(entitylivingbase);

            if (d0 < 4.0D) {
                if (attackTime <= 0) {
                    attackTime = 20;
                    entityLord.attackEntityAsMob(entitylivingbase);
                }

                this.entityLord.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY,
                        entitylivingbase.posZ, 1.0D);
            } else if (d0 < getFollowDistance() * getFollowDistance()) {
                double d1 = entitylivingbase.posX - entityLord.posX;
                double d2 = entitylivingbase.getEntityBoundingBox().minY + (double) (entitylivingbase.height / 2.0F) -
                        (entityLord.posY + (double) (entityLord.height / 2.0F));
                double d3 = entitylivingbase.posZ - entityLord.posZ;

                if (attackTime <= 0) {
                    ++attackStep;

                    if (attackStep == 1) {
                        attackTime = 20;
                        entityLord.setOnFire(false);
                    } else if (attackStep <= 4) {
                        attackTime = 8;
                    } else {
                        attackTime = 20;
                        attackStep = 0;
                        entityLord.setOnFire(false);
                    }

                    if (attackStep > 1) {
                        float f = MathHelper.sqrt(MathHelper.sqrt(d0)) * 0.5F;
                        entityLord.world.playEvent(null, 1018,
                                new BlockPos((int) entityLord.posX, (int) entityLord.posY, (int) entityLord.posZ), 0);

                        for (int i = 0; i < 1; ++i) {
                            EntityWitherSkull entitysmallfireball = new EntityWitherSkull(entityLord.world, entityLord,
                                    d1 + entityLord.getRNG().nextGaussian() * (double) f, d2,
                                    d3 + entityLord.getRNG().nextGaussian() * (double) f);
                            entitysmallfireball.posY = entityLord.posY + (double) (entityLord.height / 2.0F) + 0.5D;
                            entityLord.world.spawnEntity(entitysmallfireball);
                        }
                    }
                }

                entityLord.getLookHelper().setLookPositionWithEntity(entitylivingbase, 10.0F, 10.0F);
            } else {
                entityLord.getNavigator().clearPath();
                entityLord.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY,
                        entitylivingbase.posZ, 1.0D);
            }

            super.updateTask();
        }

        private double getFollowDistance() {
            IAttributeInstance iattributeinstance = this.entityLord
                    .getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
            return Objects.isNull(iattributeinstance) ? 64.0D : iattributeinstance.getAttributeValue();
        }
    }
}
