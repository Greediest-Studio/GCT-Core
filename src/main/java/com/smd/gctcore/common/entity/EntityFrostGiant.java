package com.smd.gctcore.common.entity;

import com.smd.gctcore.misc.PotionsItemRegistry;
import com.smd.gctcore.misc.SoundRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/** A hostile, immovable giant whose two telegraphed blows resolve on the server clock. */
public class EntityFrostGiant extends EntityAnimatedGiant {

    public static final byte UNARMED_SLAM = 1;
    public static final byte HAMMER_SWING = 2;
    public static final int SLAM_TICKS = 29;
    public static final int SLAM_HIT_TICK = 10;
    public static final int HAMMER_TICKS = 80; // User-edited swing_hammer: 4.0 seconds.
    public static final int HAMMER_HIT_TICK = 56;
    public static final int IMMOBILIZE_TICKS = 60;
    private static final int SLAM_FACING_LOCK_TICK = 4;
    private static final int HAMMER_FACING_LOCK_TICK = 40;
    private static final double SLAM_REACH = 4.0D;
    private static final double HAMMER_REACH = 6.0D;
    private static final float HAMMER_KNOCKBACK = 2.0F;
    private boolean facingLocked;
    private float lockedYaw;

    public EntityFrostGiant(World world) {
        super(world);
        setSize(2.0F, 5.0F);
        stepHeight = 1.0F;
        experienceValue = 20;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(130.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(5.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(9.0D);
    }

    @Override
    protected String getGiantFaction() {
        return "frost_giant";
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> type) {
        return !EntityFrostGiant.class.isAssignableFrom(type) && super.canAttackClass(type);
    }

    @Override
    protected double getMeleeReach() {
        return HAMMER_REACH;
    }

    @Override
    protected String getAnimationPrefix() {
        return "animation.frost_giant.";
    }

    @Override
    protected String getActionAnimation(byte action) {
        return action == HAMMER_SWING ? "swing_hammer" : "unarmed_slam";
    }

    @Override
    protected int getActionLength(byte action) {
        return action == HAMMER_SWING ? HAMMER_TICKS : SLAM_TICKS;
    }

    @Override
    protected int getActionImpactTick(byte action) {
        return action == HAMMER_SWING ? HAMMER_HIT_TICK : SLAM_HIT_TICK;
    }

    @Override
    protected byte chooseAction(EntityLivingBase target) {
        double distanceSq = getDistanceSq(target);
        if (distanceSq > HAMMER_REACH * HAMMER_REACH || !getEntitySenses().canSee(target)) {
            return IDLE;
        }
        return distanceSq <= SLAM_REACH * SLAM_REACH && rand.nextBoolean()
                ? UNARMED_SLAM : HAMMER_SWING;
    }

    @Override
    protected void onActionStarted(byte action, EntityLivingBase target) {
        facingLocked = false;
        playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.1F, 0.6F);
    }

    private int getFacingLockTick(byte action) {
        return action == HAMMER_SWING ? HAMMER_FACING_LOCK_TICK : SLAM_FACING_LOCK_TICK;
    }

    @Override
    protected boolean shouldTrackTargetDuringAction(byte action, int elapsed) {
        return elapsed < getFacingLockTick(action);
    }

    @Override
    protected void onActionTick(byte action, int elapsed, EntityLivingBase target) {
        if (elapsed >= getFacingLockTick(action)) {
            if (!facingLocked) {
                lockedYaw = rotationYaw;
                facingLocked = true;
            }
            // Keep the visible body and the damage sector facing the same direction.
            rotationYaw = lockedYaw;
            renderYawOffset = lockedYaw;
        }
    }

    @Override
    protected void resolveAction(byte action, EntityLivingBase target) {
        if (action == HAMMER_SWING) {
            resolveHammerSwing();
        } else {
            playSound(SoundEvents.BLOCK_STONE_FALL, 1.5F, 0.55F);
            // Damage is delayed; moving out, around the fist, or behind cover avoids it.
            if (canMeleeHit(target, SLAM_REACH, 75.0F) && hurtWithoutKnockback(target, 9.0F)
                    && target instanceof EntityPlayer && target.isEntityAlive()
                    && PotionsItemRegistry.IMMOBILIZED != null) {
                target.addPotionEffect(new PotionEffect(
                        PotionsItemRegistry.IMMOBILIZED, IMMOBILIZE_TICKS, 0, false, true));
                syncVelocity(target);
            }
        }
    }

    private void resolveHammerSwing() {
        playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.8F, 0.5F);
        // Broad phase only: each victim is checked again against the six-block
        // sphere, locked front half-plane and line of sight at the impact tick.
        for (EntityLivingBase victim : world.getEntitiesWithinAABB(EntityLivingBase.class,
                getEntityBoundingBox().grow(HAMMER_REACH))) {
            if (canMeleeHit(victim, HAMMER_REACH, 90.0F) && hurtTarget(victim, 15.0F)) {
                // Strong enough to carry an unresisting target beyond five blocks
                // in open space; terrain, Forge hooks and resistance still apply.
                knockAway(victim, HAMMER_KNOCKBACK);
            }
        }
    }

    private boolean hurtWithoutKnockback(EntityLivingBase target, float damage) {
        double motionX = target.motionX;
        double motionY = target.motionY;
        double motionZ = target.motionZ;
        boolean airborne = target.isAirBorne;
        boolean hit;
        try {
            hit = hurtTarget(target, damage);
        } finally {
            // LivingEntity damage applies vanilla knockback before returning.
            // Restore every axis, including vertical impulse, for this fist only.
            target.motionX = motionX;
            target.motionY = motionY;
            target.motionZ = motionZ;
            target.isAirBorne = airborne;
            target.velocityChanged = true;
            syncVelocity(target);
        }
        return hit;
    }

    private void syncVelocity(EntityLivingBase target) {
        if (target instanceof EntityPlayerMP && ((EntityPlayerMP) target).connection != null) {
            ((EntityPlayerMP) target).connection.sendPacket(new SPacketEntityVelocity(target));
        }
    }

    @Override
    public void knockBack(Entity source, float strength, double xRatio, double zRatio) {
        // A modified attribute must not allow the giant to be knocked back either.
    }

    @Override
    public void addVelocity(double x, double y, double z) {
        // Also reject direct external impulses, including explosion knockback.
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.FROST_GIANT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.FROST_GIANT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.FROST_GIANT_DEATH;
    }
}
