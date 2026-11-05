package io.github.srdjanv.endreforked.common.entity;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

public abstract class EndEntity extends EntityMob {

    protected static final UUID ATTACKING_SPEED_BOOST_ID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    protected static final AttributeModifier ATTACKING_SPEED_BOOST = (new AttributeModifier(ATTACKING_SPEED_BOOST_ID,
            "Attacking speed boost", 0.15000000596046448D, 0)).setSaved(false);
    protected static final DataParameter<Boolean> SCREAMING = EntityDataManager.createKey(EndEntity.class,
            DataSerializers.BOOLEAN);

    protected int lifetime;
    protected int lastCreepySound;
    protected int targetChangeTime;

    protected EndEntity(World worldIn) {
        super(worldIn);
        this.setSize(0.6F, 2.9F);
        this.stepHeight = 1.0F;
        this.setPathPriority(PathNodeType.WATER, -1.0F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SCREAMING, Boolean.FALSE);
    }

    @Override
    public void onLivingUpdate() {
        if (this.world.isRemote) {
            for (int i = 0; i < 2; ++i) {
                this.world.spawnParticle(EnumParticleTypes.PORTAL,
                        this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width,
                        this.posY + this.rand.nextDouble() * (double) this.height - 0.25D,
                        this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width,
                        (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(),
                        (this.rand.nextDouble() - 0.5D) * 2.0D);
            }
        }

        this.isJumping = false;
        super.onLivingUpdate();

        if (!world.isRemote) {
            if (!isNoDespawnRequired()) ++lifetime;
            if (lifetime >= 1200) setDead();
        }
    }

    @Override
    protected void updateAITasks() {
        if (isWet()) attackEntityFrom(DamageSource.DROWN, 1.0F);

        if (world.isDaytime() && ticksExisted >= targetChangeTime + 600) {
            float f = getBrightness();

            if (f > 0.5F && world.canSeeSky(new BlockPos(this)) &&
                    rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F) {
                setAttackTarget(null);
                teleportRandomly();
            }
        }

        super.updateAITasks();
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entityTarget) {
        super.setAttackTarget(entityTarget);
        IAttributeInstance iattributeinstance = this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        if (entityTarget == null) {
            targetChangeTime = 0;
            dataManager.set(SCREAMING, Boolean.FALSE);
            iattributeinstance.removeModifier(ATTACKING_SPEED_BOOST);
        } else {
            targetChangeTime = ticksExisted;
            dataManager.set(SCREAMING, Boolean.TRUE);

            if (!iattributeinstance.hasModifier(ATTACKING_SPEED_BOOST)) {
                iattributeinstance.applyModifier(ATTACKING_SPEED_BOOST);
            }
        }
    }

    @Override
    public void notifyDataManagerChange(@NotNull DataParameter<?> key) {
        if (SCREAMING.equals(key) && this.isScreaming() && this.world.isRemote) {
            playEndermanSound();
        }

        super.notifyDataManagerChange(key);
    }

    @Override
    public void writeEntityToNBT(@NotNull NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Lifetime", lifetime);
    }

    @Override
    public void readEntityFromNBT(@NotNull NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.lifetime = compound.getInteger("Lifetime");
    }

    protected boolean teleportRandomly() {
        double d0 = this.posX + (this.rand.nextDouble() - 0.5D) * 64.0D;
        double d1 = this.posY + (double) (this.rand.nextInt(64) - 32);
        double d2 = this.posZ + (this.rand.nextDouble() - 0.5D) * 64.0D;
        return this.teleportTo(d0, d1, d2);
    }

    protected boolean teleportToEntity(Entity entity) {
        Vec3d vec3d = new Vec3d(this.posX - entity.posX, this.getEntityBoundingBox().minY +
                (double) (this.height / 2.0F) - entity.posY + (double) entity.getEyeHeight(),
                this.posZ - entity.posZ);
        vec3d = vec3d.normalize();
        double d1 = this.posX + (this.rand.nextDouble() - 0.5D) * 8.0D - vec3d.x * 16.0D;
        double d2 = this.posY + (double) (this.rand.nextInt(16) - 8) - vec3d.y * 16.0D;
        double d3 = this.posZ + (this.rand.nextDouble() - 0.5D) * 8.0D - vec3d.z * 16.0D;
        return this.teleportTo(d1, d2, d3);
    }

    protected boolean teleportTo(double x, double y, double z) {
        net.minecraftforge.event.entity.living.EnderTeleportEvent event = new net.minecraftforge.event.entity.living.EnderTeleportEvent(
                this, x, y, z, 0);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return false;
        boolean flag = this.attemptTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ());

        if (flag) {
            this.world.playSound(null, this.prevPosX, this.prevPosY, this.prevPosZ,
                    SoundEvents.ENTITY_ENDERMEN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
        }

        return flag;
    }

    protected boolean shouldAttackPlayer(EntityPlayer player) {
        ItemStack itemstack = player.inventory.armorInventory.get(3);

        if (itemstack.getItem() == Item.getItemFromBlock(Blocks.PUMPKIN)) {
            return false;
        } else {
            Vec3d vec3d = player.getLook(1.0F).normalize();
            Vec3d vec3d1 = new Vec3d(this.posX - player.posX, this.getEntityBoundingBox().minY +
                    (double) this.getEyeHeight() - (player.posY + (double) player.getEyeHeight()),
                    this.posZ - player.posZ);
            double d0 = vec3d1.length();
            vec3d1 = vec3d1.normalize();
            double d1 = vec3d.dotProduct(vec3d1);
            return d1 > 1.0D - 0.025D / d0 && player.canEntityBeSeen(this);
        }
    }

    @Override
    public boolean attackEntityFrom(@NotNull DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else if (source instanceof EntityDamageSourceIndirect) {
            for (int i = 0; i < 64; ++i) {
                if (this.teleportRandomly()) {
                    return true;
                }
            }

            return false;
        } else {
            boolean flag = super.attackEntityFrom(source, amount);

            if (source.isUnblockable() && this.rand.nextInt(10) != 0) {
                this.teleportRandomly();
            }

            return flag;
        }
    }

    public boolean isScreaming() {
        return this.dataManager.get(SCREAMING);
    }

    protected void playEndermanSound() {
        if (this.ticksExisted >= this.lastCreepySound + 400) {
            this.lastCreepySound = this.ticksExisted;

            if (!this.isSilent()) {
                this.world.playSound(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ,
                        SoundEvents.ENTITY_ENDERMEN_STARE, this.getSoundCategory(), 2.5F, 1.0F, false);
            }
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && this.world.canSeeSky(new BlockPos(this));
    }

    protected static class AIFindLookingPlayer<E extends EndEntity>
                                              extends EntityAINearestAttackableTarget<EntityPlayer> {

        private final E entity;
        private EntityPlayer player;
        private int aggroTime;
        private int teleportTime;

        public AIFindLookingPlayer(E entity) {
            super(entity, EntityPlayer.class, false);
            this.entity = entity;
        }

        @Override
        public boolean shouldExecute() {
            double distance = this.getTargetDistance();
            player = this.entity.world.getNearestAttackablePlayer(this.entity.posX, this.entity.posY,
                    this.entity.posZ, distance, distance, null,
                    entityPlayer -> entityPlayer != null && entity.shouldAttackPlayer(entityPlayer));
            return player != null;
        }

        @Override
        public void startExecuting() {
            this.aggroTime = 5;
            this.teleportTime = 0;
        }

        @Override
        public void resetTask() {
            this.player = null;
            super.resetTask();
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (this.player != null) {
                if (!this.entity.shouldAttackPlayer(this.player)) {
                    return false;
                } else {
                    this.entity.faceEntity(this.player, 10.0F, 10.0F);
                    return true;
                }
            } else
                return this.targetEntity != null && this.targetEntity.isEntityAlive() ||
                        super.shouldContinueExecuting();
        }

        @Override
        public void updateTask() {
            if (this.player != null) {
                if (--aggroTime <= 0) {
                    targetEntity = player;
                    player = null;
                    super.startExecuting();
                }
            } else {
                if (targetEntity != null) {
                    if (entity.shouldAttackPlayer(this.targetEntity)) {
                        if (targetEntity.getDistanceSq(this.entity) < 16.0D) {
                            entity.teleportRandomly();
                        }

                        teleportTime = 0;
                    } else if (targetEntity.getDistanceSq(this.entity) > 256.0D &&
                            teleportTime++ >= 30 && entity.teleportToEntity(targetEntity)) {
                                teleportTime = 0;
                            }
                }

                super.updateTask();
            }
        }
    }

    protected static class MobEntityAIFollow<E extends EndEntity> extends EntityAIBase {

        private final E endEntity;
        private EntityPlayer followingPlayer;
        private final PathNavigate navigation;
        private final float minDistance = 5;
        private final float maxDistance = 8;
        private final double speedModifier = 1.2;
        private int timeToRecalcPath;

        private int stareTime;

        public MobEntityAIFollow(E endEntity) {
            this.endEntity = endEntity;
            navigation = endEntity.navigator;
        }

        protected double getTargetDistance() {
            IAttributeInstance iattributeinstance = this.endEntity
                    .getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
            return Objects.isNull(iattributeinstance) ? 16.0D : iattributeinstance.getAttributeValue();
        }

        @Override
        public boolean shouldExecute() {
            double distance = getTargetDistance();
            followingPlayer = endEntity.world.getNearestAttackablePlayer(
                    endEntity.posX, endEntity.posY, endEntity.posZ,
                    distance, distance, null,
                    entityPlayer -> Objects.nonNull(entityPlayer) && endEntity.shouldAttackPlayer(entityPlayer));
            return followingPlayer != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (Objects.nonNull(followingPlayer)) {
                var distance = endEntity.getDistanceSq(followingPlayer);

                if (distance > (double) (maxDistance * maxDistance)) {
                    return true;
                } else if (distance > (double) (minDistance * minDistance))
                    return (++stareTime % 120) != 0;
            }
            return false;
        }

        @Override
        public void startExecuting() {
            timeToRecalcPath = 0;
        }

        @Override
        public void resetTask() {
            followingPlayer = null;
            stareTime = 0;
            navigation.clearPath();
        }

        @Override
        public void updateTask() {
            if (Objects.isNull(followingPlayer) || endEntity.getLeashed()) return;
            endEntity.getLookHelper().setLookPositionWithEntity(followingPlayer, 10.0F,
                    (float) endEntity.getVerticalFaceSpeed());

            if (--timeToRecalcPath <= 0) {
                timeToRecalcPath = 10;
                var distance = endEntity.getDistanceSq(followingPlayer);

                if (distance > (double) (maxDistance * maxDistance)) {
                    navigation.tryMoveToEntityLiving(followingPlayer, speedModifier);
                } else if (distance > (double) (minDistance * minDistance)) {
                    navigation.clearPath();
                }
            }
        }
    }
}
