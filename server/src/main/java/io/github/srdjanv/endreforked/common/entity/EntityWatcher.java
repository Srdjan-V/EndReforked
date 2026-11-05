package io.github.srdjanv.endreforked.common.entity;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import io.github.srdjanv.endreforked.common.LootHandler;
import io.github.srdjanv.endreforked.common.configs.Configs;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class EntityWatcher extends EndEntity {

    protected static final DataParameter<Integer> TIMES_ESCAPED = EntityDataManager.createKey(EntityWatcher.class,
            DataSerializers.VARINT);
    protected static final DataParameter<Boolean> HUNT_PLAYER = EntityDataManager.createKey(EntityWatcher.class,
            DataSerializers.BOOLEAN);
    protected final int huntCooldown;
    protected final int maxEscapes;

    public EntityWatcher(World worldIn) {
        this(worldIn,
                Configs.SERVER_SIDE_CONFIGS.ENTITY_CONFIGS.WATCHER.max_escapes,
                Configs.SERVER_SIDE_CONFIGS.ENTITY_CONFIGS.WATCHER.hunt_cooldown);
    }

    public EntityWatcher(World worldIn, int maxEscapes, int huntCooldown) {
        super(worldIn);
        this.maxEscapes = maxEscapes;
        this.huntCooldown = huntCooldown;
        dataManager.register(TIMES_ESCAPED, 0);
        dataManager.register(HUNT_PLAYER, false);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAIAttackMelee(this, 1.0D, false));
        tasks.addTask(1, new EntityAIKeepDistanceAndWatch(this));
        tasks.addTask(3, new EntityAIWanderAvoidWater(this, 1.0D, 0.0F));
        targetTasks.addTask(1, new EntityAIHurtByTargetReinforce(this));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, 10, true, false,
                player -> EntityWatcher.this.dataManager.get(HUNT_PLAYER)));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.34D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.34D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0D);
    }

    @Override
    protected void updateAITasks() {
        if (ticksExisted >= targetChangeTime + huntCooldown) {
            dataManager.set(TIMES_ESCAPED, 0);
            dataManager.set(HUNT_PLAYER, false);
        }

        if (isWet()) attackEntityFrom(DamageSource.DROWN, 1.0F);
        if (!dataManager.get(HUNT_PLAYER) && world.isDaytime() && ticksExisted >= targetChangeTime + 60 * 20) {
            float brightness = getBrightness();
            if (brightness > 0.5F && world.canSeeSky(new BlockPos(this)) &&
                    rand.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F) {
                setAttackTarget(null);
                teleportRandomly();
            }
        }
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LootHandler.WATCHER;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);

        if (flag && getHeldItemMainhand().isEmpty() && entityIn instanceof EntityLivingBase) {
            float f = world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();
            ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 140 * (int) f));
        }

        return flag;
    }

    @Override
    public float getEyeHeight() {
        return 2.55F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isScreaming() ? SoundEvents.ENTITY_ENDERMEN_SCREAM : SoundEvents.ENTITY_ENDERMEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_ENDERMEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDERMEN_DEATH;
    }

    protected static class EntityAIHurtByTargetReinforce extends EntityAIHurtByTarget {

        public EntityAIHurtByTargetReinforce(EntityCreature creatureIn) {
            super(creatureIn, true);
        }

        @Override
        protected void alertOthers() {
            if (Objects.isNull(taskOwner.getRevengeTarget())) return;// should not be possible
            double d0 = getTargetDistance();
            AxisAlignedBB searchArea = (new AxisAlignedBB(
                    taskOwner.posX, taskOwner.posY, taskOwner.posZ,
                    taskOwner.posX + 1.0D, taskOwner.posY + 1.0D, taskOwner.posZ + 1.0D))
                            .grow(d0, 10.0D, d0);
            List<EntityMob> entityMobs = new ObjectArrayList<>();
            entityMobs.addAll(taskOwner.world.getEntitiesWithinAABB(EntityEnderman.class, searchArea));
            entityMobs.addAll(taskOwner.world.getEntitiesWithinAABB(EntityWatcher.class, searchArea));

            for (EntityCreature entitycreature : entityMobs) {
                if (entitycreature instanceof EntityWatcher watcher) watcher.dataManager.set(HUNT_PLAYER, true);
                if (taskOwner != entitycreature && entitycreature.getAttackTarget() == null)
                    setEntityAttackTarget(entitycreature, taskOwner.getRevengeTarget());
            }
        }
    }

    protected static class EntityAIKeepDistanceAndWatch extends EntityAIBase {

        private final EntityWatcher watcher;
        private EntityPlayer followingPlayer;
        private final PathNavigate navigation;
        private final float minPlayerDis = 8;
        private final float buffPlayerDis = 4;
        private final int maxFailedEscapes = 3;
        private final int maxTPPosSearch = 32;
        private int failedEscapes = 0;
        private int timeToRecalcPath;
        private boolean follow = true;

        public EntityAIKeepDistanceAndWatch(EntityWatcher watcher) {
            this.watcher = watcher;
            this.navigation = watcher.getNavigator();
        }

        protected double getWatcherFollowRange() {
            IAttributeInstance attribute = watcher.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
            return Objects.isNull(attribute) ? 16.0D : attribute.getAttributeValue();
        }

        @Override
        public boolean shouldExecute() {
            if (watcher.getDataManager().get(TIMES_ESCAPED) > watcher.maxEscapes ||
                    watcher.getDataManager().get(HUNT_PLAYER)) {
                watcher.getDataManager().set(HUNT_PLAYER, true);
                return false;
            }
            double followRange = getWatcherFollowRange();
            followingPlayer = watcher.world.getNearestAttackablePlayer(
                    watcher.posX, watcher.posY, watcher.posZ,
                    followRange, followRange, null, null);
            return followingPlayer != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (Objects.isNull(followingPlayer)) return false;
            if (watcher.getAttackingEntity() != null) return false;
            if (watcher.getDataManager().get(HUNT_PLAYER)) return false;
            if (watcher.getDataManager().get(TIMES_ESCAPED) > watcher.maxEscapes) return false;

            var distance = watcher.getDistance(followingPlayer);
            var watcherRange = getWatcherFollowRange();
            if (distance > watcherRange) {
                return false;
            }
            if (distance > minPlayerDis + buffPlayerDis) {
                follow = true;
                return true;
            }
            if (distance > minPlayerDis) {
                follow = false;
                return true;
            }

            // try to escape player
            var newPos = new BlockPos.MutableBlockPos();
            for (int i = 0; i < maxTPPosSearch; i++) {
                double x = followingPlayer.posX + (watcher.rand.nextDouble() - 0.5D) * watcherRange;
                double y = followingPlayer.posY + (watcher.rand.nextDouble() - 0.5D) * watcherRange;
                double z = followingPlayer.posZ + (watcher.rand.nextDouble() - 0.5D) * watcherRange;
                newPos.setPos(x, y, z);

                var newDistance = followingPlayer.getPosition().getDistance(newPos.getX(), newPos.getY(),
                        newPos.getZ());

                if (newDistance > watcherRange) continue;
                if (newDistance < minPlayerDis) continue;
                double orgX = watcher.posX;
                double orgY = watcher.posY;
                double orgZ = watcher.posZ;
                var orgPath = navigation.getPath();

                watcher.posX = x;
                watcher.posY = y;
                watcher.posZ = z;
                navigation.clearPath();
                var posNav = navigation.getPathToPos(followingPlayer.getPosition());

                if (posNav == null) {
                    watcher.posX = orgX;
                    watcher.posY = orgY;
                    watcher.posZ = orgZ;
                    navigation.setPath(orgPath, 1);
                    continue;
                }
                watcher.posX = orgX;
                watcher.posY = orgY;
                watcher.posZ = orgZ;
                var startPathPoint = posNav.getPathPointFromIndex(0);
                if (!watcher.teleportTo(startPathPoint.x, startPathPoint.y, startPathPoint.z)) continue;

                follow = true;
                watcher.dataManager.set(TIMES_ESCAPED, watcher.getDataManager().get(TIMES_ESCAPED) + 1);
                return true;
            }
            if (failedEscapes > maxFailedEscapes) {
                watcher.getDataManager().set(TIMES_ESCAPED, 0);
                watcher.getDataManager().set(HUNT_PLAYER, true);
                failedEscapes = 0;
            } else failedEscapes++;
            return false;
        }

        @Override
        public void startExecuting() {
            timeToRecalcPath = 0;
        }

        @Override
        public void resetTask() {
            followingPlayer = null;
            navigation.clearPath();
        }

        @Override
        public void updateTask() {
            if (Objects.isNull(followingPlayer) || watcher.getLeashed()) return;
            watcher.getLookHelper().setLookPositionWithEntity(followingPlayer, 10.0F,
                    (float) watcher.getVerticalFaceSpeed());

            // var watcherRange = getWatcherFollowRange();
            if (!follow) {
                navigation.clearPath();
                return;
            }
            if (--timeToRecalcPath <= 0) {
                timeToRecalcPath = 10;
                var distance = watcher.getDistance(followingPlayer);

                if (distance > minPlayerDis + buffPlayerDis) {
                    double speedModifier = 1.2;
                    navigation.tryMoveToEntityLiving(followingPlayer, speedModifier);
                } else if (distance > minPlayerDis) {
                    navigation.clearPath();
                }
            }
        }
    }
}
