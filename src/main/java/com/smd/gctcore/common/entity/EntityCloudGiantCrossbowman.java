package com.smd.gctcore.common.entity;

import com.smd.gctcore.misc.SoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

/** A crossbow infantryman whose projectiles remain physical and can be dodged. */
public class EntityCloudGiantCrossbowman extends EntityMob implements IAnimatable, ICloudGiantAlly {

    public static final byte IDLE = 0;
    public static final byte RELOAD = 1;
    public static final byte AIM = 2;
    public static final byte SHOOT = 3;
    public static final int RELOAD_TICKS = 42;
    public static final int AIM_TICKS = 24;
    public static final int SHOOT_TICKS = 20;
    public static final int SHOT_RELEASE_TICK = 5;
    public static final double ACQUIRE_RANGE = 24.0D;
    public static final double AGGRO_RANGE = 128.0D;
    private static final String ANIMATION_PREFIX = "animation.cloud_giant_crossbowman.";
    private static final DataParameter<Byte> ACTION = EntityDataManager.createKey(
            EntityCloudGiantCrossbowman.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> ACTION_SEQUENCE = EntityDataManager.createKey(
            EntityCloudGiantCrossbowman.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ACTION_START = EntityDataManager.createKey(
            EntityCloudGiantCrossbowman.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> BOLT_LOADED = EntityDataManager.createKey(
            EntityCloudGiantCrossbowman.class, DataSerializers.BOOLEAN);

    private final AnimationFactory animationFactory = new AnimationFactory(this);
    private EntityLivingBase actionTarget;
    private int actionTicks;
    private int lastRevengeTimer;
    private int displayedActionSequence = Integer.MIN_VALUE;
    private byte displayedAction = -1;

    public EntityCloudGiantCrossbowman(World world) {
        super(world);
        setSize(1.0F, 4.0F);
        stepHeight = 1.0F;
        experienceValue = 10;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ACTION, IDLE);
        dataManager.register(ACTION_SEQUENCE, 0);
        dataManager.register(ACTION_START, 0);
        dataManager.register(BOLT_LOADED, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(120.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.345D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(AGGRO_RANGE);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new CombatTask());
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.8D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 24.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        // Acquisition deliberately uses 24 blocks; only an existing aggro target uses 128.
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
                && world.getEntityByID(target.getEntityId()) == target
                && !isOnSameTeam(target) && getDistanceSq(target) <= AGGRO_RANGE * AGGRO_RANGE
                && (!(target instanceof EntityPlayer) || !((EntityPlayer) target).isSpectator());
    }

    private void updateCombatTarget() {
        if (getRevengeTimer() != lastRevengeTimer) {
            lastRevengeTimer = getRevengeTimer();
            if (isValidCombatTarget(getRevengeTarget())) {
                setAttackTarget(getRevengeTarget());
            }
        }
        if (isValidCombatTarget(getAttackTarget())) {
            return;
        }
        setAttackTarget(null);
        if (ticksExisted % 10 != 0) {
            return;
        }
        double nearestDistance = ACQUIRE_RANGE * ACQUIRE_RANGE;
        EntityPlayer nearest = null;
        for (EntityPlayer player : world.playerEntities) {
            if (!isValidCombatTarget(player) || player.capabilities.disableDamage
                    || !getEntitySenses().canSee(player)) {
                continue;
            }
            double distance = getDistanceSq(player);
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }
        setAttackTarget(nearest);
    }

    public byte getAction() {
        return dataManager.get(ACTION);
    }

    public boolean isPerformingAction() {
        return getAction() != IDLE;
    }

    public boolean isReloading() {
        return getAction() == RELOAD;
    }

    public boolean isAiming() {
        return getAction() == AIM;
    }

    public boolean isShooting() {
        return getAction() == SHOOT;
    }

    public boolean isBoltLoaded() {
        return dataManager.get(BOLT_LOADED);
    }

    private void beginAction(byte action, EntityLivingBase target) {
        actionTarget = target;
        actionTicks = 0;
        dataManager.set(ACTION_START, (int) world.getTotalWorldTime());
        dataManager.set(ACTION, action);
        dataManager.set(ACTION_SEQUENCE, dataManager.get(ACTION_SEQUENCE) + 1);
        getNavigator().clearPath();
        setMoveForward(0.0F);
        setMoveStrafing(0.0F);
        if (action == RELOAD) {
            playSound(SoundRegistry.CLOUD_GIANT_CROSSBOWMAN_RELOAD, 1.0F, 0.8F);
        }
    }

    private void clearAction() {
        actionTarget = null;
        actionTicks = 0;
        dataManager.set(ACTION, IDLE);
    }

    private Vec3d getMuzzlePosition(EntityLivingBase target) {
        double dx = target.posX - posX;
        double dz = target.posZ - posZ;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 0.0001D) {
            double yaw = Math.toRadians(rotationYaw);
            dx = -Math.sin(yaw);
            dz = Math.cos(yaw);
            length = 1.0D;
        }
        // Center of the authored loaded bolt at the shoot animation's release pose.
        return new Vec3d(posX + dx / length * 0.90D, posY + 3.22D, posZ + dz / length * 0.90D);
    }

    private Vec3d getAimPosition(EntityLivingBase target) {
        return new Vec3d(target.posX,
                target.posY + Math.min(target.getEyeHeight(), target.height * 0.7D), target.posZ);
    }

    private boolean hasClearShot(EntityLivingBase target) {
        if (!isValidCombatTarget(target) || !getEntitySenses().canSee(target)) {
            return false;
        }
        Vec3d muzzle = getMuzzlePosition(target);
        Vec3d chest = new Vec3d(posX, muzzle.y, posZ);
        return world.rayTraceBlocks(chest, muzzle, false, true, false) == null
                && world.rayTraceBlocks(muzzle, getAimPosition(target), false, true, false) == null;
    }

    private void releaseBolt(EntityLivingBase target) {
        if (!isBoltLoaded() || !hasClearShot(target)
                || target instanceof EntityPlayer && ((EntityPlayer) target).isCreative()) {
            return;
        }
        Vec3d muzzle = getMuzzlePosition(target);
        Vec3d velocity = EntityCloudGiantBolt.getLaunchVelocity(getAimPosition(target).subtract(muzzle));
        if (velocity == null) {
            return;
        }
        // The projectile follows the current target bearing. Face that same bearing at
        // release, so a last-moment crossing does not make the bolt fire sideways.
        rotationYaw = (float) (MathHelper.atan2(velocity.z, velocity.x) * (180.0D / Math.PI)) - 90.0F;
        rotationYawHead = renderYawOffset = rotationYaw;
        EntityCloudGiantBolt bolt = new EntityCloudGiantBolt(world, this);
        bolt.setPosition(muzzle.x, muzzle.y, muzzle.z);
        bolt.shoot(velocity.x, velocity.y, velocity.z, EntityCloudGiantBolt.SHOT_SPEED, 0.0F);
        if (world.spawnEntity(bolt)) {
            dataManager.set(BOLT_LOADED, false);
            playSound(SoundRegistry.CLOUD_GIANT_CROSSBOWMAN_SHOOT, 1.5F, 0.8F);
        }
    }

    private void tickAction() {
        byte action = getAction();
        if (action == IDLE) {
            return;
        }
        if (!isValidCombatTarget(actionTarget)) {
            clearAction();
            return;
        }
        ++actionTicks;
        // Keep the loaded bolt and chase if a wall blocks the windup or release.
        if ((action == AIM || action == SHOOT && actionTicks <= SHOT_RELEASE_TICK)
                && !hasClearShot(actionTarget)) {
            clearAction();
            return;
        }
        if (action == RELOAD && actionTicks >= RELOAD_TICKS) {
            dataManager.set(BOLT_LOADED, true);
            if (hasClearShot(actionTarget)) {
                beginAction(AIM, actionTarget);
            } else {
                clearAction();
            }
        } else if (action == AIM && actionTicks >= AIM_TICKS) {
            beginAction(SHOOT, actionTarget);
        } else if (action == SHOOT) {
            if (actionTicks == SHOT_RELEASE_TICK) {
                releaseBolt(actionTarget);
            }
            if (actionTicks >= SHOOT_TICKS) {
                if (!isBoltLoaded()) {
                    beginAction(RELOAD, actionTarget);
                } else {
                    clearAction();
                }
            }
        }
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote && isEntityAlive() && !isAIDisabled()) {
            updateCombatTarget();
            tickAction();
        }
        super.onLivingUpdate();
        if (isAiming() || isShooting()) {
            // Vanilla's stationary body helper can trail head/rotationYaw by 75 degrees.
            // The authored crossbow belongs to the torso, so keep its render yaw aligned.
            renderYawOffset = rotationYaw;
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (isPerformingAction() || !isEntityAlive()) {
            strafe = forward = 0.0F;
            motionX = motionZ = 0.0D;
        }
        super.travel(strafe, vertical, forward);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        // The crossbow is this unit's only attack; collisions never produce instant melee damage.
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !(source.getTrueSource() instanceof ICloudGiantAlly) && super.attackEntityFrom(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.CLOUD_GIANT_CROSSBOWMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.CLOUD_GIANT_CROSSBOWMAN_DEATH;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("CloudGiantBoltLoaded", isBoltLoaded());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        dataManager.set(BOLT_LOADED, compound.getBoolean("CloudGiantBoltLoaded"));
        clearAction();
    }

    private PlayState animationPredicate(AnimationEvent<EntityCloudGiantCrossbowman> event) {
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
        String animation;
        switch (action) {
            case RELOAD:
                animation = "reload";
                break;
            case AIM:
                animation = "aim";
                break;
            case SHOOT:
                animation = "shoot";
                break;
            default:
                animation = event.isMoving() ? "walk" : "idle";
        }
        EDefaultLoopTypes loop = action == IDLE ? EDefaultLoopTypes.LOOP : EDefaultLoopTypes.PLAY_ONCE;
        event.getController().setAnimation(new AnimationBuilder().addAnimation(ANIMATION_PREFIX + animation, loop));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<EntityCloudGiantCrossbowman>(
                this, "body", 0, this::animationPredicate) {
            @Override
            protected double adjustTick(double tick) {
                double adjustedTick = super.adjustTick(tick);
                if (getAnimationState() == AnimationState.Running && isPerformingAction()) {
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
            return isEntityAlive() && (isPerformingAction() || isValidCombatTarget(getAttackTarget()));
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = isPerformingAction() ? actionTarget : getAttackTarget();
            if (isValidCombatTarget(target)) {
                getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                faceEntity(target, 20.0F, 20.0F);
            }
            if (isPerformingAction()) {
                getNavigator().clearPath();
                setMoveForward(0.0F);
                setMoveStrafing(0.0F);
                return;
            }
            if (!isValidCombatTarget(target)) {
                return;
            }
            if (!isBoltLoaded()) {
                beginAction(RELOAD, target);
            } else if (hasClearShot(target)) {
                beginAction(AIM, target);
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
