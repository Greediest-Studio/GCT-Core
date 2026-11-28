package com.smd.gctcore.common.entity;

import com.smd.gctcore.misc.SoundRegistry;
import com.smd.gctcore.misc.GiantLootTables;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Neutral cave giant: a close rock fist and a deliberately dodgeable arcing boulder. */
public class EntityStoneGiant extends EntityAnimatedGiant {

    public static final byte MELEE = 1;
    public static final byte BOULDER_THROW = 2;
    public static final int MELEE_TICKS = 41;
    public static final int MELEE_HIT_TICK = 22;
    public static final int THROW_TICKS = 49;
    public static final int THROW_RELEASE_TICK = 28;
    private static final int MELEE_COOLDOWN = 52;
    private static final int THROW_COOLDOWN = 120;
    private static final double MELEE_REACH = 5.5D;
    private static final double THROW_MIN_SQ = 8.0D * 8.0D;
    private static final double THROW_MAX_SQ = 32.0D * 32.0D;
    private int meleeReadyAt;
    private int throwReadyAt;

    public EntityStoneGiant(World world) {
        super(world);
        setSize(1.8F, 5.5F);
        stepHeight = 1.0F;
        experienceValue = 20;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return GiantLootTables.STONE;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(160.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.6D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
    }

    @Override
    protected boolean isNeutralGiant() {
        return true;
    }

    @Override
    protected String getGiantFaction() {
        return "stone_giant";
    }

    @Override
    protected double getAcquireRange() {
        return 64.0D;
    }

    @Override
    protected double getMeleeReach() {
        return MELEE_REACH;
    }

    @Override
    protected String getAnimationPrefix() {
        return "animation.stone_giant.";
    }

    @Override
    protected String getActionAnimation(byte action) {
        return action == BOULDER_THROW ? "boulder_throw" : "melee_swing";
    }

    @Override
    protected int getActionLength(byte action) {
        return action == BOULDER_THROW ? THROW_TICKS : MELEE_TICKS;
    }

    @Override
    protected int getActionImpactTick(byte action) {
        return action == BOULDER_THROW ? THROW_RELEASE_TICK : MELEE_HIT_TICK;
    }

    @Override
    protected byte chooseAction(EntityLivingBase target) {
        double distanceSq = getDistanceSq(target);
        if (distanceSq <= MELEE_REACH * MELEE_REACH && ticksExisted >= meleeReadyAt
                && getEntitySenses().canSee(target)) {
            return MELEE;
        }
        if (distanceSq >= THROW_MIN_SQ && distanceSq <= THROW_MAX_SQ && ticksExisted >= throwReadyAt
                && getEntitySenses().canSee(target)) {
            return BOULDER_THROW;
        }
        return 0;
    }

    @Override
    protected void onActionStarted(byte action, EntityLivingBase target) {
        if (action == BOULDER_THROW) {
            throwReadyAt = ticksExisted + THROW_COOLDOWN;
            playSound(SoundEvents.BLOCK_STONE_PLACE, 1.0F, 0.6F);
        } else {
            meleeReadyAt = ticksExisted + MELEE_COOLDOWN;
            playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.0F, 0.7F);
        }
    }

    @Override
    protected void resolveAction(byte action, EntityLivingBase target) {
        if (action == BOULDER_THROW) {
            throwBoulder(target);
        } else if (canMeleeHit(target, MELEE_REACH, 75.0F) && hurtTarget(target, 12.0F)) {
            knockAway(target, 0.65F);
            playSound(SoundEvents.BLOCK_STONE_HIT, 1.1F, 0.65F);
        }
    }

    private void throwBoulder(EntityLivingBase target) {
        if (!isValidCombatTarget(target) || !canHarm(target) || !getEntitySenses().canSee(target)
                || getDistanceSq(target) < THROW_MIN_SQ || getDistanceSq(target) > THROW_MAX_SQ) {
            return;
        }
        double yaw = Math.toRadians(renderYawOffset);
        Vec3d launch = new Vec3d(posX + Math.cos(yaw) * 0.9D - Math.sin(yaw) * 0.7D,
                posY + height * 0.72D, posZ + Math.sin(yaw) * 0.9D + Math.cos(yaw) * 0.7D);
        Vec3d aim = new Vec3d(target.posX, target.posY + target.height * 0.5D, target.posZ);
        Vec3d velocity = EntityStoneGiantBoulder.getLaunchVelocity(aim.subtract(launch));
        if (velocity == null) {
            return;
        }
        EntityStoneGiantBoulder boulder = new EntityStoneGiantBoulder(world, this);
        // The entity's origin is its bottom; launch/ballistic aiming use the rock's center.
        boulder.setPosition(launch.x, launch.y - boulder.height * 0.5D, launch.z);
        boulder.shoot(velocity.x, velocity.y, velocity.z, EntityStoneGiantBoulder.SHOT_SPEED, 0.0F);
        if (world.spawnEntity(boulder)) {
            playSound(SoundEvents.ENTITY_SNOWBALL_THROW, 1.5F, 0.45F);
        }
    }

    @Override
    protected void updateCombatMovement(EntityLivingBase target) {
        double distanceSq = getDistanceSq(target);
        if (distanceSq >= THROW_MIN_SQ && distanceSq <= THROW_MAX_SQ && ticksExisted >= throwReadyAt
                && getEntitySenses().canSee(target)) {
            stopWalking();
        } else if (distanceSq <= MELEE_REACH * MELEE_REACH) {
            stopWalking();
        } else if (ticksExisted % 5 == 0) {
            getNavigator().tryMoveToEntityLiving(target, 1.0D);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.STONE_GIANT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.STONE_GIANT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.STONE_GIANT_DEATH;
    }
}
