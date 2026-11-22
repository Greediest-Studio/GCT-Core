package com.smd.gctcore.common.entity;

import com.smd.gctcore.misc.PotionsItemRegistry;
import com.smd.gctcore.misc.SoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/** Heavy infantry whose telegraphed club attacks resolve on the server clock. */
public class EntityHillGiant extends EntityMob implements IAnimatable {

    public static final byte IDLE = 0;
    public static final byte SWEEP = 1;
    public static final byte SLAM = 2;
    public static final int SWEEP_TICKS = 41; // horizontal_swing: 2.05 seconds
    public static final int SWEEP_HIT_TICK = 25;
    public static final int SLAM_TICKS = 46; // vertical_slam: 2.30 seconds
    public static final int SLAM_HIT_TICK = 30;
    public static final int IMMOBILIZE_TICKS = 60;
    private static final double SWEEP_REACH_SQ = 36.0D;
    private static final double SLAM_REACH_SQ = 25.0D;
    private static final String ANIMATION_PREFIX = "animation.hill_giant.";
    private static final DataParameter<Byte> ACTION = EntityDataManager.createKey(
            EntityHillGiant.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> ACTION_SEQUENCE = EntityDataManager.createKey(
            EntityHillGiant.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ACTION_START = EntityDataManager.createKey(
            EntityHillGiant.class, DataSerializers.VARINT);

    private final AnimationFactory animationFactory = new AnimationFactory(this);
    private EntityLivingBase actionTarget;
    private boolean actionResolved;
    private int displayedActionSequence = Integer.MIN_VALUE;
    private byte displayedAction = -1;

    public EntityHillGiant(World world) {
        super(world);
        setSize(2.0F, 5.0F);
        stepHeight = 1.0F;
        experienceValue = 15;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ACTION, IDLE);
        dataManager.register(ACTION_SEQUENCE, 0);
        dataManager.register(ACTION_START, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(120.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(5.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new CombatTask());
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.8D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    public boolean isOnSameTeam(Entity entity) {
        return entity instanceof EntityHillGiant || super.isOnSameTeam(entity);
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> type) {
        return !EntityHillGiant.class.isAssignableFrom(type) && super.canAttackClass(type);
    }

    @Override
    public void setAttackTarget(EntityLivingBase target) {
        super.setAttackTarget(target instanceof EntityHillGiant ? null : target);
    }

    @Override
    public void setRevengeTarget(EntityLivingBase target) {
        super.setRevengeTarget(target instanceof EntityHillGiant ? null : target);
    }

    private boolean isValidCombatTarget(EntityLivingBase target) {
        return target != null && target != this && target.world == world && target.isEntityAlive()
                && world.getEntityByID(target.getEntityId()) == target && !isOnSameTeam(target)
                && (!(target instanceof EntityPlayer) || !((EntityPlayer) target).isSpectator());
    }

    public byte getAction() {
        return dataManager.get(ACTION);
    }

    private boolean isAttacking() {
        return getAction() != IDLE;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (world.isRemote || !isEntityAlive() || isAttacking() || !(target instanceof EntityLivingBase)
                || !isValidCombatTarget((EntityLivingBase) target)
                || getDistanceSq(target) > SWEEP_REACH_SQ || !getEntitySenses().canSee(target)) {
            return false;
        }
        // Beyond five blocks only the longer sweep can reach the target.
        byte action = getDistanceSq(target) <= SLAM_REACH_SQ && rand.nextBoolean() ? SLAM : SWEEP;
        actionTarget = (EntityLivingBase) target;
        actionResolved = false;
        dataManager.set(ACTION_START, (int) world.getTotalWorldTime());
        dataManager.set(ACTION, action);
        dataManager.set(ACTION_SEQUENCE, dataManager.get(ACTION_SEQUENCE) + 1);
        stopWalking();
        return true;
    }

    private void stopWalking() {
        getNavigator().clearPath();
        setMoveForward(0.0F);
        setMoveStrafing(0.0F);
    }

    private void resolveAttack(byte action) {
        playSound(action == SLAM ? SoundEvents.BLOCK_WOOD_FALL : SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                1.6F, 0.6F);
        double reachSq = action == SLAM ? SLAM_REACH_SQ : SWEEP_REACH_SQ;
        // Recheck at impact: an out-of-range target or a target behind a wall is safe.
        if (!isValidCombatTarget(actionTarget) || getDistanceSq(actionTarget) > reachSq
                || !getEntitySenses().canSee(actionTarget)
                || actionTarget instanceof EntityPlayer && ((EntityPlayer) actionTarget).isCreative()) {
            return;
        }
        DamageSource source = new EntityDamageSource("mob", this) {
            @Override
            public boolean isDifficultyScaled() {
                return false;
            }
        };
        float damage = action == SLAM ? 15.0F : 7.0F;
        if (actionTarget.attackEntityFrom(source, damage)) {
            setLastAttackedEntity(actionTarget);
            if (action == SLAM && actionTarget instanceof EntityPlayer && actionTarget.isEntityAlive()) {
                actionTarget.addPotionEffect(new PotionEffect(
                        PotionsItemRegistry.IMMOBILIZED, IMMOBILIZE_TICKS, 0, false, true));
            }
        }
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote) {
            if (!isEntityAlive()) {
                dataManager.set(ACTION, IDLE);
                actionTarget = null;
            } else if (isAttacking()) {
                byte action = getAction();
                int elapsed = (int) world.getTotalWorldTime() - dataManager.get(ACTION_START);
                int hitTick = action == SLAM ? SLAM_HIT_TICK : SWEEP_HIT_TICK;
                if (!actionResolved && elapsed >= hitTick) {
                    actionResolved = true;
                    resolveAttack(action);
                }
                if (elapsed >= (action == SLAM ? SLAM_TICKS : SWEEP_TICKS)) {
                    dataManager.set(ACTION, IDLE);
                    actionTarget = null;
                }
            }
        }
        super.onLivingUpdate();
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (isAttacking() || !isEntityAlive()) {
            strafe = forward = 0.0F;
            motionX = motionZ = 0.0D;
        }
        super.travel(strafe, vertical, forward);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.HILL_GIANT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.HILL_GIANT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.HILL_GIANT_DEATH;
    }

    private PlayState animationPredicate(AnimationEvent<EntityHillGiant> event) {
        if (!isEntityAlive()) {
            return PlayState.STOP;
        }
        byte action = getAction();
        int sequence = dataManager.get(ACTION_SEQUENCE);
        if (sequence != displayedActionSequence || action != displayedAction) {
            event.getController().markNeedsReload();
            displayedActionSequence = sequence;
            displayedAction = action;
        }
        String name = action == SLAM ? "vertical_slam" : action == SWEEP ? "horizontal_swing"
                : event.isMoving() ? "walk" : "idle";
        EDefaultLoopTypes loop = isAttacking() ? EDefaultLoopTypes.PLAY_ONCE : EDefaultLoopTypes.LOOP;
        event.getController().setAnimation(new AnimationBuilder().addAnimation(ANIMATION_PREFIX + name, loop));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<EntityHillGiant>(
                this, "body", 0, this::animationPredicate) {
            @Override
            protected double adjustTick(double tick) {
                double adjustedTick = super.adjustTick(tick);
                if (getAnimationState() == AnimationState.Running && isAttacking()) {
                    int elapsed = (int) world.getTotalWorldTime() - dataManager.get(ACTION_START);
                    return Math.max(0, elapsed) + tick - Math.floor(tick);
                }
                return adjustedTick;
            }
        });
    }

    @Override
    public AnimationFactory getFactory() {
        return animationFactory;
    }

    private class CombatTask extends EntityAIBase {
        CombatTask() {
            setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return isEntityAlive() && (isAttacking() || isValidCombatTarget(getAttackTarget()));
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = isAttacking() ? actionTarget : getAttackTarget();
            if (isValidCombatTarget(target)) {
                getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                faceEntity(target, 20.0F, 20.0F);
            }
            if (isAttacking()) {
                stopWalking();
                return;
            }
            if (!isValidCombatTarget(target)) {
                return;
            }
            if (getDistanceSq(target) <= SWEEP_REACH_SQ && getEntitySenses().canSee(target)) {
                attackEntityAsMob(target);
            } else if (ticksExisted % 5 == 0) {
                getNavigator().tryMoveToEntityLiving(target, 1.0D);
            }
        }

        @Override
        public void resetTask() {
            getNavigator().clearPath();
        }
    }
}
