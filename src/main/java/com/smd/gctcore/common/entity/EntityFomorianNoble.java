package com.smd.gctcore.common.entity;

import com.smd.gctcore.misc.SoundRegistry;
import com.smd.gctcore.misc.GiantLootTables;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** A mobile fomorian spellcaster whose strongest attacks have avoidable tells. */
public class EntityFomorianNoble extends EntityAnimatedGiant {

    public static final byte MELEE = 1;
    public static final byte FIREBALL = 2;
    public static final byte ICE_CONE = 3;
    public static final byte CHAIN_LIGHTNING = 4;

    private static final double MELEE_REACH = 4.5D;
    private static final double CONE_REACH = 10.0D;
    private static final double CONE_DOT = Math.cos(Math.toRadians(30.0D));
    private static final int CONE_LOCK_TICK = 19;
    private static final int LIGHTNING_LOCK_TICK = 15;
    private int nextMeleeTick;
    private int nextFireballTick;
    private int nextIceTick;
    private int nextLightningTick;
    private int nextSpellTick;
    private Vec3d coneDirection = new Vec3d(0.0D, 0.0D, 1.0D);
    private boolean lightningLocked;

    public EntityFomorianNoble(World world) {
        super(world);
        setSize(1.6F, 5.0F);
        stepHeight = 1.0F;
        experienceValue = 30;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return GiantLootTables.FOMORIAN_NOBLE;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(220.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.30D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.30D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(80.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(16.0D);
    }

    @Override
    protected String getAnimationPrefix() {
        return "animation.fomorian_noble.";
    }

    @Override
    protected String getActionAnimation(byte action) {
        return action == MELEE ? "melee_attack" : "spell_cast";
    }

    @Override
    protected int getActionLength(byte action) {
        return action == MELEE ? 41 : 50;
    }

    @Override
    protected int getActionImpactTick(byte action) {
        return action == MELEE ? 22 : 29;
    }

    @Override
    protected double getMeleeReach() {
        return MELEE_REACH;
    }

    @Override
    protected double getAcquireRange() {
        return 32.0D;
    }

    @Override
    protected String getGiantFaction() {
        return "fomorian";
    }

    @Override
    protected byte chooseAction(EntityLivingBase target) {
        if (!isValidCombatTarget(target) || !getEntitySenses().canSee(target)) {
            return 0;
        }
        double distanceSq = getDistanceSq(target);
        boolean spellReady = ticksExisted >= nextSpellTick;
        if (spellReady && distanceSq <= CONE_REACH * CONE_REACH && ticksExisted >= nextIceTick) {
            return ICE_CONE;
        }
        if (distanceSq <= MELEE_REACH * MELEE_REACH && ticksExisted >= nextMeleeTick) {
            return MELEE;
        }
        if (!spellReady) {
            return 0;
        }
        boolean fireballReady = distanceSq <= 28.0D * 28.0D && ticksExisted >= nextFireballTick;
        boolean lightningReady = distanceSq <= 24.0D * 24.0D && ticksExisted >= nextLightningTick;
        if (fireballReady && lightningReady) {
            return rand.nextBoolean() ? FIREBALL : CHAIN_LIGHTNING;
        }
        return fireballReady ? FIREBALL : lightningReady ? CHAIN_LIGHTNING : 0;
    }

    @Override
    protected void onActionStarted(byte action, EntityLivingBase target) {
        if (action == MELEE) {
            nextMeleeTick = ticksExisted + 60;
            return;
        }
        nextSpellTick = ticksExisted + 80;
        if (action == FIREBALL) {
            nextFireballTick = ticksExisted + 160;
        } else if (action == ICE_CONE) {
            nextIceTick = ticksExisted + 200;
            updateConeDirection(target);
        } else if (action == CHAIN_LIGHTNING) {
            nextLightningTick = ticksExisted + 280;
            lightningLocked = false;
        }
        playSound(SoundEvents.EVOCATION_ILLAGER_PREPARE_ATTACK, 1.2F, 0.65F);
    }

    @Override
    protected void onActionTick(byte action, int elapsed, EntityLivingBase target) {
        if (action == ICE_CONE) {
            if (elapsed <= CONE_LOCK_TICK) {
                updateConeDirection(target);
            }
            lockConeFacing();
            if (elapsed < 29 && elapsed % 4 == 0) {
                showConeWarning();
            }
        } else if (action == CHAIN_LIGHTNING) {
            if (elapsed < LIGHTNING_LOCK_TICK && isValidCombatTarget(target)) {
                actionPoint = target.getPositionVector();
            }
            if (!lightningLocked && elapsed >= LIGHTNING_LOCK_TICK) {
                if (isValidCombatTarget(target)) {
                    Vec3d displacement = target.getPositionVector().subtract(getPositionVector());
                    if (displacement.lengthSquared() > 24.0D * 24.0D) {
                        displacement = displacement.normalize().scale(24.0D);
                    }
                    actionPoint = getPositionVector().add(displacement);
                }
                lightningLocked = true;
            }
            if (elapsed < 29 && elapsed % 2 == 1) {
                telegraph(actionPoint, 2.0D, EnumParticleTypes.CRIT_MAGIC);
            }
        } else if (action == FIREBALL && elapsed < 29 && elapsed % 4 == 0) {
            particles(EnumParticleTypes.FLAME,
                    getPositionVector().add(0.0D, getEyeHeight() * 0.8D, 0.0D), 8);
        }
    }

    @Override
    protected boolean shouldTrackTargetDuringAction(byte action, int elapsed) {
        return action != ICE_CONE || elapsed < CONE_LOCK_TICK;
    }

    private void updateConeDirection(EntityLivingBase target) {
        if (!isValidCombatTarget(target)) {
            return;
        }
        Vec3d offset = new Vec3d(target.posX - posX, 0.0D, target.posZ - posZ);
        if (offset.lengthSquared() > 1.0E-6D) {
            coneDirection = offset.normalize();
        }
    }

    private void lockConeFacing() {
        float yaw = (float) (Math.toDegrees(Math.atan2(coneDirection.z, coneDirection.x)) - 90.0D);
        rotationYaw = yaw;
        rotationYawHead = yaw;
        renderYawOffset = yaw;
    }

    private void showConeWarning() {
        double baseAngle = Math.atan2(coneDirection.z, coneDirection.x);
        for (int index = 0; index <= 8; index++) {
            double angle = baseAngle + Math.toRadians(-30.0D + index * 7.5D);
            particles(EnumParticleTypes.SNOW_SHOVEL,
                    new Vec3d(posX + Math.cos(angle) * CONE_REACH, posY + 0.2D,
                            posZ + Math.sin(angle) * CONE_REACH), 2);
        }
    }

    @Override
    protected void resolveAction(byte action, EntityLivingBase target) {
        if (action == MELEE) {
            playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.5F, 0.65F);
            if (canMeleeHit(target, MELEE_REACH, 60.0F) && hurtTarget(target, 16.0F)) {
                knockAway(target, 0.65F);
            }
        } else if (action == FIREBALL) {
            if (isValidCombatTarget(target) && getDistanceSq(target) <= 28.0D * 28.0D
                    && getEntitySenses().canSee(target)) {
                EntityGiantSpell.launch(this, EntityGiantSpell.FIREBALL, target, 1.3F);
                playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.5F, 0.75F);
            }
        } else if (action == ICE_CONE) {
            resolveIceCone();
        } else if (action == CHAIN_LIGHTNING) {
            resolveLightning();
        }
    }

    private void resolveIceCone() {
        playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.6F, 1.4F);
        double baseAngle = Math.atan2(coneDirection.z, coneDirection.x);
        for (int distance = 2; distance <= 10; distance += 2) {
            for (int index = 0; index <= 6; index++) {
                double angle = baseAngle + Math.toRadians(-30.0D + index * 10.0D);
                particles(EnumParticleTypes.SNOW_SHOVEL,
                        new Vec3d(posX + Math.cos(angle) * distance, posY + 1.2D,
                                posZ + Math.sin(angle) * distance), 4);
            }
        }
        AxisAlignedBB bounds = getEntityBoundingBox().grow(CONE_REACH, 2.0D, CONE_REACH);
        for (EntityLivingBase victim : world.getEntitiesWithinAABB(EntityLivingBase.class, bounds)) {
            if (!canHarm(victim) || getDistanceSq(victim) > CONE_REACH * CONE_REACH
                    || Math.abs(victim.posY - posY) > 4.5D || !getEntitySenses().canSee(victim)) {
                continue;
            }
            Vec3d offset = new Vec3d(victim.posX - posX, 0.0D, victim.posZ - posZ);
            if (offset.lengthSquared() > 1.0E-6D && offset.normalize().dotProduct(coneDirection) < CONE_DOT) {
                continue;
            }
            if (hurtTarget(victim, 12.0F)) {
                victim.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 0, false, true));
            }
        }
    }

    private void resolveLightning() {
        if (actionPoint == null) {
            return;
        }
        world.addWeatherEffect(new EntityLightningBolt(world, actionPoint.x, actionPoint.y,
                actionPoint.z, true));
        world.playSound(null, actionPoint.x, actionPoint.y, actionPoint.z,
                SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.HOSTILE, 2.0F, 1.05F);
        EntityLivingBase primary = closestAtImpact();
        if (primary == null) {
            return;
        }
        List<EntityLivingBase> struck = new ArrayList<>();
        struck.add(primary);
        if (!hurtTarget(primary, 14.0F)) { return; }
        EntityLivingBase previous = primary;
        for (int jump = 0; jump < 2; jump++) {
            EntityLivingBase next = nextLightningTarget(previous, struck);
            if (next == null) {
                break;
            }
            showLightningChain(previous, next);
            hurtTarget(next, 8.0F);
            struck.add(next);
            previous = next;
        }
    }

    @Nullable
    private EntityLivingBase closestAtImpact() {
        AxisAlignedBB impactBox = new AxisAlignedBB(actionPoint.x - 2.0D, actionPoint.y - 2.0D,
                actionPoint.z - 2.0D, actionPoint.x + 2.0D, actionPoint.y + 4.0D,
                actionPoint.z + 2.0D);
        EntityLivingBase closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (EntityLivingBase candidate : world.getEntitiesWithinAABB(EntityLivingBase.class, impactBox)) {
            if (!canHarm(candidate) || !getEntitySenses().canSee(candidate)) {
                continue;
            }
            double dx = candidate.posX - actionPoint.x;
            double dz = candidate.posZ - actionPoint.z;
            double distance = dx * dx + dz * dz;
            if (distance <= 4.0D && distance < closestDistance) {
                closest = candidate;
                closestDistance = distance;
            }
        }
        return closest;
    }

    @Nullable
    private EntityLivingBase nextLightningTarget(EntityLivingBase previous,
                                                 List<EntityLivingBase> struck) {
        EntityLivingBase closest = null;
        double closestDistance = 25.0D;
        for (EntityLivingBase candidate : world.getEntitiesWithinAABB(EntityLivingBase.class,
                previous.getEntityBoundingBox().grow(5.0D))) {
            if (!canHarm(candidate) || struck.contains(candidate)) {
                continue;
            }
            double distance = previous.getDistanceSq(candidate);
            if (distance <= closestDistance && world.rayTraceBlocks(previous.getPositionEyes(1.0F),
                    candidate.getPositionEyes(1.0F), false, true, false) == null) {
                closest = candidate;
                closestDistance = distance;
            }
        }
        return closest;
    }

    private void showLightningChain(EntityLivingBase from, EntityLivingBase to) {
        Vec3d start = from.getPositionEyes(1.0F);
        Vec3d difference = to.getPositionEyes(1.0F).subtract(start);
        int steps = Math.max(2, (int) (difference.length() * 4.0D));
        for (int step = 0; step <= steps; step++) {
            particles(EnumParticleTypes.CRIT_MAGIC, start.add(difference.scale((double) step / steps)), 2);
        }
    }

    @Override
    protected void updateCombatMovement(EntityLivingBase target) {
        keepDistance(target, 10.0D, 18.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.FOMORIAN_NOBLE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.FOMORIAN_NOBLE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.FOMORIAN_NOBLE_DEATH;
    }
}
