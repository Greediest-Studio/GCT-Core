package com.smd.gctcore.common.entity;

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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/** Shared, server-timed attacks. Damage never depends on a client's animation callback. */
public abstract class EntityAnimatedGiant extends EntityMob implements IAnimatable {
    public static final byte IDLE = 0;
    private static final DataParameter<Byte> ACTION = EntityDataManager.createKey(
            EntityAnimatedGiant.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> SEQUENCE = EntityDataManager.createKey(
            EntityAnimatedGiant.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> START = EntityDataManager.createKey(
            EntityAnimatedGiant.class, DataSerializers.VARINT);
    private final AnimationFactory animationFactory = new AnimationFactory(this);
    private EntityLivingBase actionTarget;
    protected Vec3d actionPoint = Vec3d.ZERO;
    private boolean resolved;
    private int displayedSequence = Integer.MIN_VALUE;
    private byte displayedAction = -1;

    protected EntityAnimatedGiant(World world) {
        super(world);
        stepHeight = 1.0F;
        experienceValue = 25;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ACTION, IDLE);
        dataManager.register(SEQUENCE, 0);
        dataManager.register(START, 0);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new CombatTask());
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.7D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        if (!isNeutralGiant()) {
            targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(
                    this, EntityPlayer.class, true) {
                @Override
                protected double getTargetDistance() {
                    return getAttackTarget() == null ? getAcquireRange()
                            : EntityAnimatedGiant.this.getEntityAttribute(
                                    SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
                }
            });
        }
    }

    protected boolean isNeutralGiant() { return false; }
    protected double getAcquireRange() { return 32.0D; }
    protected String getGiantFaction() { return getClass().getName(); }
    protected abstract String getAnimationPrefix();
    protected abstract String getActionAnimation(byte action);
    protected abstract int getActionLength(byte action);
    protected abstract int getActionImpactTick(byte action);
    protected abstract byte chooseAction(EntityLivingBase target);
    protected abstract void resolveAction(byte action, EntityLivingBase target);
    protected abstract double getMeleeReach();
    protected void onActionStarted(byte action, EntityLivingBase target) { }
    protected void onActionTick(byte action, int elapsed, EntityLivingBase target) { }
    protected boolean shouldTrackTargetDuringAction(byte action, int elapsed) { return true; }

    public byte getAction() { return dataManager.get(ACTION); }
    public boolean isPerformingAction() { return getAction() != IDLE; }
    protected int getActionElapsed() {
        return (int) world.getTotalWorldTime() - dataManager.get(START);
    }
    protected EntityLivingBase getActionTarget() { return actionTarget; }

    @Override
    public boolean isOnSameTeam(Entity other) {
        return other instanceof EntityAnimatedGiant
                && getGiantFaction().equals(((EntityAnimatedGiant) other).getGiantFaction())
                || super.isOnSameTeam(other);
    }

    @Override
    public void setAttackTarget(EntityLivingBase target) {
        super.setAttackTarget(target != null && isOnSameTeam(target) ? null : target);
    }

    @Override
    public void setRevengeTarget(EntityLivingBase target) {
        super.setRevengeTarget(target != null && isOnSameTeam(target) ? null : target);
    }

    public static boolean isProtectedPlayer(Entity entity) {
        return entity instanceof EntityPlayer && (((EntityPlayer) entity).isSpectator()
                || ((EntityPlayer) entity).isCreative()
                || ((EntityPlayer) entity).capabilities.disableDamage);
    }

    protected boolean isValidCombatTarget(EntityLivingBase target) {
        return target != null && target != this && target.world == world && target.isEntityAlive()
                && world.getEntityByID(target.getEntityId()) == target && !isOnSameTeam(target)
                && (!(target instanceof EntityPlayer) || !((EntityPlayer) target).isSpectator());
    }

    protected boolean canHarm(EntityLivingBase target) {
        return isValidCombatTarget(target) && !isProtectedPlayer(target);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getTrueSource() != null && isOnSameTeam(source.getTrueSource())) { return false; }
        return super.attackEntityFrom(source, amount);
    }

    protected boolean hurtTarget(EntityLivingBase target, float damage) {
        if (world.isRemote || !canHarm(target)) { return false; }
        DamageSource source = new EntityDamageSource("mob", this) {
            @Override
            public boolean isDifficultyScaled() { return false; }
        };
        boolean hit = target.attackEntityFrom(source, damage);
        if (hit) { setLastAttackedEntity(target); }
        return hit;
    }

    protected boolean canMeleeHit(EntityLivingBase target, double reach, float halfAngleDegrees) {
        if (!canHarm(target) || getDistanceSq(target) > reach * reach
                || !getEntitySenses().canSee(target)) { return false; }
        Vec3d direction = target.getPositionVector().subtract(getPositionVector());
        double horizontal = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        if (horizontal < 1.0E-6D) { return true; }
        double yaw = Math.toRadians(rotationYaw);
        return (-Math.sin(yaw) * direction.x + Math.cos(yaw) * direction.z) / horizontal
                >= Math.cos(Math.toRadians(halfAngleDegrees));
    }

    protected void knockAway(EntityLivingBase target, float strength) {
        applyGiantKnockback(this, target, target.posX - posX, target.posZ - posZ, strength);
    }

    public static void applyGiantKnockback(Entity source, EntityLivingBase target,
                                           double dx, double dz, float strength) {
        if (target.world.isRemote || isProtectedPlayer(target)) { return; }
        if (dx * dx + dz * dz < 1.0E-8D) { dx = 1.0D; }
        LivingKnockBackEvent event = ForgeHooks.onLivingKnockBack(target, source, strength, -dx, -dz);
        if (event.isCanceled()) { return; }
        double resistance = MathHelper.clamp(target.getEntityAttribute(
                SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue(), 0.0D, 1.0D);
        double impulse = Math.max(0.0D, event.getStrength()) * (1.0D - resistance);
        double length = Math.sqrt(event.getRatioX() * event.getRatioX()
                + event.getRatioZ() * event.getRatioZ());
        if (impulse <= 0.0D || length < 1.0E-8D) { return; }
        target.motionX = target.motionX * 0.5D - event.getRatioX() / length * impulse;
        target.motionZ = target.motionZ * 0.5D - event.getRatioZ() / length * impulse;
        target.motionY += Math.min(0.35D, impulse * 0.28D);
        target.isAirBorne = true;
        target.velocityChanged = true;
        if (target instanceof EntityPlayerMP) {
            ((EntityPlayerMP) target).connection.sendPacket(new SPacketEntityVelocity(target));
        }
    }

    protected boolean startAction(byte action, EntityLivingBase target) {
        if (world.isRemote || action == IDLE || !isEntityAlive() || isPerformingAction()
                || !isValidCombatTarget(target) || !getEntitySenses().canSee(target)) { return false; }
        actionTarget = target;
        actionPoint = target.getPositionVector();
        resolved = false;
        dataManager.set(START, (int) world.getTotalWorldTime());
        dataManager.set(ACTION, action);
        dataManager.set(SEQUENCE, dataManager.get(SEQUENCE) + 1);
        stopWalking();
        onActionStarted(action, target);
        return true;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (!(target instanceof EntityLivingBase)) { return false; }
        return startAction(chooseAction((EntityLivingBase) target), (EntityLivingBase) target);
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote) {
            if (!isEntityAlive()) {
                dataManager.set(ACTION, IDLE);
                actionTarget = null;
            } else if (isPerformingAction()) {
                byte action = getAction();
                int elapsed = getActionElapsed();
                onActionTick(action, elapsed, actionTarget);
                if (!resolved && elapsed >= getActionImpactTick(action)) {
                    resolved = true;
                    resolveAction(action, actionTarget);
                }
                if (elapsed >= getActionLength(action)) {
                    dataManager.set(ACTION, IDLE);
                    actionTarget = null;
                }
            }
        }
        super.onLivingUpdate();
    }

    protected void stopWalking() {
        getNavigator().clearPath();
        setMoveForward(0.0F);
        setMoveStrafing(0.0F);
    }

    protected void updateCombatMovement(EntityLivingBase target) {
        if (getDistanceSq(target) <= getMeleeReach() * getMeleeReach()
                && getEntitySenses().canSee(target)) {
            stopWalking();
        } else if (ticksExisted % 5 == 0) {
            getNavigator().tryMoveToEntityLiving(target, 1.0D);
        }
    }

    /** Let the ground navigator validate retreat paths; never strafe directly over an island edge. */
    protected void keepDistance(EntityLivingBase target, double min, double max) {
        double distanceSq = getDistanceSq(target);
        if (distanceSq > max * max || !getEntitySenses().canSee(target)) {
            if (ticksExisted % 10 == 0) {
                getNavigator().tryMoveToEntityLiving(target, 1.0D);
            }
        } else if (distanceSq < min * min) {
            if (ticksExisted % 10 != 0) { return; }
            Vec3d away = new Vec3d(posX - target.posX, 0.0D, posZ - target.posZ).normalize();
            for (int offset : new int[] {0, 45, -45, 90, -90}) {
                Vec3d direction = away.rotateYaw((float) Math.toRadians(offset));
                BlockPos feet = new BlockPos(posX + direction.x * 4.0D, posY, posZ + direction.z * 4.0D);
                if (!world.isBlockLoaded(feet) || !world.getBlockState(feet.down()).isFullCube()
                        || world.getBlockState(feet.down()).getMaterial().isLiquid()) { continue; }
                Path path = getNavigator().getPathToPos(feet);
                if (path != null && path.getFinalPathPoint() != null
                        && path.getFinalPathPoint().distanceTo(new net.minecraft.pathfinding.PathPoint(
                                feet.getX(), feet.getY(), feet.getZ())) < 2.0F) {
                    getNavigator().setPath(path, 0.85D);
                    return;
                }
            }
            stopWalking();
        } else {
            stopWalking();
        }
    }

    protected void particles(EnumParticleTypes type, Vec3d position, int count) {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(type, position.x, position.y, position.z,
                    count, 0.2D, 0.2D, 0.2D, 0.02D);
        }
    }

    protected void telegraph(Vec3d center, double radius, EnumParticleTypes type) {
        if (!(world instanceof WorldServer)) { return; }
        for (int i = 0; i < 20; i++) {
            double angle = i * Math.PI * 2.0D / 20.0D;
            ((WorldServer) world).spawnParticle(type,
                    center.x + Math.cos(angle) * radius, center.y + 0.15D,
                    center.z + Math.sin(angle) * radius, 1, 0.0D, 0.03D, 0.0D, 0.0D);
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

    private PlayState animationPredicate(AnimationEvent<EntityAnimatedGiant> event) {
        if (!isEntityAlive()) { return PlayState.STOP; }
        byte action = getAction();
        int sequence = dataManager.get(SEQUENCE);
        if (sequence != displayedSequence || action != displayedAction) {
            event.getController().markNeedsReload();
            displayedSequence = sequence;
            displayedAction = action;
        }
        String name = isPerformingAction() ? getActionAnimation(action) : event.isMoving() ? "walk" : "idle";
        event.getController().setAnimation(new AnimationBuilder().addAnimation(getAnimationPrefix() + name,
                isPerformingAction() ? EDefaultLoopTypes.PLAY_ONCE : EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<EntityAnimatedGiant>(
                this, "body", 0, this::animationPredicate) {
            @Override
            protected double adjustTick(double tick) {
                double adjusted = super.adjustTick(tick);
                if (getAnimationState() == AnimationState.Running && isPerformingAction()) {
                    return Math.max(0, getActionElapsed()) + tick - Math.floor(tick);
                }
                return adjusted;
            }
        });
    }

    @Override
    public AnimationFactory getFactory() { return animationFactory; }

    private class CombatTask extends EntityAIBase {
        CombatTask() { setMutexBits(3); }
        @Override
        public boolean shouldExecute() {
            return isEntityAlive() && (isPerformingAction() || isValidCombatTarget(getAttackTarget()));
        }
        @Override
        public boolean shouldContinueExecuting() { return shouldExecute(); }
        @Override
        public void updateTask() {
            EntityLivingBase target = isPerformingAction() ? actionTarget : getAttackTarget();
            if (isValidCombatTarget(target) && (!isPerformingAction()
                    || shouldTrackTargetDuringAction(getAction(), getActionElapsed()))) {
                getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                faceEntity(target, 20.0F, 20.0F);
            }
            if (isPerformingAction()) {
                stopWalking();
            } else if (isValidCombatTarget(target)) {
                byte action = chooseAction(target);
                if (!startAction(action, target)) { updateCombatMovement(target); }
            }
        }
        @Override
        public void resetTask() { getNavigator().clearPath(); }
    }
}
