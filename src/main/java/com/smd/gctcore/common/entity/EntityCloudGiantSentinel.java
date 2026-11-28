package com.smd.gctcore.common.entity;

import com.smd.gctcore.misc.SoundRegistry;
import com.smd.gctcore.misc.GiantLootTables;
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
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
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

/** Sword infantry with the priest's attack clock and a shorter melee reach. */
public class EntityCloudGiantSentinel extends EntityMob implements IAnimatable, ICloudGiantAlly {

    public static final int ATTACK_TICKS = EntityCloudGiantPriest.SWEEP_TICKS;
    public static final int ATTACK_HIT_TICK = EntityCloudGiantPriest.SWEEP_HIT_TICK;
    private static final double ATTACK_REACH_SQ = 16.0D;
    private static final String ANIMATION_PREFIX = "animation.cloud_giant_sentinel.";
    private static final DataParameter<Boolean> ATTACKING = EntityDataManager.createKey(
            EntityCloudGiantSentinel.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ACTION_SEQUENCE = EntityDataManager.createKey(
            EntityCloudGiantSentinel.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ACTION_START = EntityDataManager.createKey(
            EntityCloudGiantSentinel.class, DataSerializers.VARINT);

    private final AnimationFactory animationFactory = new AnimationFactory(this);
    private EntityLivingBase actionTarget;
    private int actionTicks;
    private int displayedActionSequence = Integer.MIN_VALUE;
    private boolean displayedAttacking;

    public EntityCloudGiantSentinel(World world) {
        super(world);
        setSize(1.0F, 4.0F);
        stepHeight = 1.0F;
        experienceValue = 10;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return GiantLootTables.CLOUD_SENTINEL;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ATTACKING, false);
        dataManager.register(ACTION_SEQUENCE, 0);
        dataManager.register(ACTION_START, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(150.0D);
        // A baby zombie uses the adult's 0.23 speed with a 50% speed bonus.
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.345D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(9.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new CombatTask());
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.8D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(2, new EntityAIGiantRivalTarget(this, 40.0D));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    public boolean isOnSameTeam(Entity entity) {
        return entity instanceof ICloudGiantAlly || super.isOnSameTeam(entity);
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> type) {
        return !ICloudGiantAlly.class.isAssignableFrom(type) && super.canAttackClass(type);
    }

    @Override
    public void setAttackTarget(EntityLivingBase target) {
        super.setAttackTarget(target instanceof ICloudGiantAlly ? null : target);
    }

    @Override
    public void setRevengeTarget(EntityLivingBase target) {
        super.setRevengeTarget(target instanceof ICloudGiantAlly ? null : target);
    }

    private boolean isValidCombatTarget(EntityLivingBase target) {
        return target != null && target != this && target.world == world && target.isEntityAlive()
                && !isOnSameTeam(target)
                && (!(target instanceof EntityPlayer) || !((EntityPlayer) target).isSpectator());
    }

    private boolean isAttacking() {
        return dataManager.get(ATTACKING);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (world.isRemote || !isEntityAlive() || isAttacking() || !(target instanceof EntityLivingBase)
                || !isValidCombatTarget((EntityLivingBase) target)
                || getDistanceSq(target) > ATTACK_REACH_SQ || !getEntitySenses().canSee(target)) {
            return false;
        }
        actionTarget = (EntityLivingBase) target;
        actionTicks = 0;
        dataManager.set(ACTION_START, (int) world.getTotalWorldTime());
        dataManager.set(ATTACKING, true);
        dataManager.set(ACTION_SEQUENCE, dataManager.get(ACTION_SEQUENCE) + 1);
        getNavigator().clearPath();
        setMoveForward(0.0F);
        setMoveStrafing(0.0F);
        playSound(SoundRegistry.CLOUD_GIANT_SENTINEL_ATTACK, 1.5F, 0.6F);
        return true;
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote && isEntityAlive() && isAttacking()) {
            ++actionTicks;
            if (actionTicks == ATTACK_HIT_TICK && isValidCombatTarget(actionTarget)
                    && getDistanceSq(actionTarget) <= ATTACK_REACH_SQ
                    && getEntitySenses().canSee(actionTarget)
                    && (!(actionTarget instanceof EntityPlayer) || !((EntityPlayer) actionTarget).isCreative())) {
                DamageSource source = new EntityDamageSource("mob", this) {
                    @Override
                    public boolean isDifficultyScaled() {
                        return false;
                    }
                };
                if (actionTarget.attackEntityFrom(source,
                        (float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue())) {
                    setLastAttackedEntity(actionTarget);
                }
            }
            if (actionTicks >= ATTACK_TICKS) {
                dataManager.set(ATTACKING, false);
                actionTarget = null;
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
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getTrueSource() instanceof ICloudGiantAlly) {
            return false;
        }
        // A hit plays audio, but never replaces or restarts an active sword animation.
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.CLOUD_GIANT_SENTINEL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.CLOUD_GIANT_SENTINEL_DEATH;
    }

    private PlayState animationPredicate(AnimationEvent<EntityCloudGiantSentinel> event) {
        if (!isEntityAlive()) {
            return PlayState.STOP;
        }
        boolean attacking = isAttacking();
        int sequence = dataManager.get(ACTION_SEQUENCE);
        if (sequence != displayedActionSequence || attacking != displayedAttacking) {
            event.getController().markNeedsReload();
            displayedActionSequence = sequence;
            displayedAttacking = attacking;
        }
        String name = attacking ? "sword_attack" : event.isMoving() ? "walk" : "idle";
        EDefaultLoopTypes loop = attacking ? EDefaultLoopTypes.PLAY_ONCE : EDefaultLoopTypes.LOOP;
        event.getController().setAnimation(new AnimationBuilder().addAnimation(ANIMATION_PREFIX + name, loop));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<EntityCloudGiantSentinel>(
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
                getNavigator().clearPath();
                setMoveForward(0.0F);
                setMoveStrafing(0.0F);
                return;
            }
            if (!isValidCombatTarget(target)) {
                return;
            }
            if (getDistanceSq(target) <= ATTACK_REACH_SQ && getEntitySenses().canSee(target)) {
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
