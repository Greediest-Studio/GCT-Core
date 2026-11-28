package com.smd.gctcore.common.entity;

import com.smd.gctcore.misc.SoundRegistry;
import com.smd.gctcore.misc.GiantLootTables;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** A delayed fist, a diagonal sword slash and a dodgeable off-hand chain at medium range. */
public class EntityFireGiant extends EntityFrostGiant {
    public static final int SLASH_TICKS = 80;
    public static final int SLASH_HIT_TICK = 56;
    public static final int SLASH_FACING_LOCK_TICK = 48;
    private static final double SLASH_REACH = 6.0D;
    private static final double SLASH_MAX_HEIGHT_DIFFERENCE = 2.5D;
    public static final byte FIRE_CHAIN = 3;
    public static final int CHAIN_TICKS = 48;
    public static final int CHAIN_RELEASE_TICK = 12;
    public static final int CHAIN_PULL_START_TICK = 20;
    public static final int CHAIN_PULL_END_TICK = 36;
    public static final int CHAIN_COOLDOWN_TICKS = 120;
    public static final double CHAIN_RANGE = 8.0D;

    // Native model-space left-hand centres sampled from fire_chain, including
    // the torso/root pose. Times are ticks; keep in step when editing this clip.
    private static final double MODEL_UNIT = (88.0D / 147.0D) / 16.0D;
    private static final double[][] CHAIN_HAND = {
            {12, -28.905665, 98.368442, -51.815654},
            {16, -28.905665, 98.368442, -51.815654},
            {18, -27.265642, 101.184771, -52.020538},
            {20, -25.671590, 104.024483, -51.793884},
            {22, -24.581790, 105.724802, -50.874297},
            {24, -23.607763, 107.344861, -49.512697},
            {25, -23.173467, 108.113925, -48.672378},
            {28, -23.296245, 106.098377, -46.504220},
            {31, -23.449905, 104.277383, -43.919855},
            {36, -23.449905, 104.277383, -43.919855}
    };

    private int chainCooldown;
    private boolean chainFacingLocked;
    private float chainYaw;
    private boolean slashFacingLocked;
    private float slashYaw;

    public EntityFireGiant(World world) {
        super(world);
        setSize(2.2F, 5.5F);
        isImmuneToFire = true;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return GiantLootTables.FIRE;
    }

    @Override
    protected String getGiantFaction() { return "fire_giant"; }

    @Override
    protected SoundEvent getAmbientSound() { return SoundRegistry.FIRE_GIANT_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundRegistry.FIRE_GIANT_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundRegistry.FIRE_GIANT_DEATH; }

    @Override
    protected String getAnimationPrefix() { return "animation.fire_giant."; }

    @Override
    protected String getActionAnimation(byte action) {
        return action == FIRE_CHAIN ? "fire_chain"
                : action == HAMMER_SWING ? "swing_weapon" : "unarmed_slam";
    }

    @Override
    protected int getActionLength(byte action) {
        return action == FIRE_CHAIN ? CHAIN_TICKS
                : action == HAMMER_SWING ? SLASH_TICKS : super.getActionLength(action);
    }

    @Override
    protected int getActionImpactTick(byte action) {
        return action == FIRE_CHAIN ? CHAIN_RELEASE_TICK
                : action == HAMMER_SWING ? SLASH_HIT_TICK : super.getActionImpactTick(action);
    }

    @Override
    protected byte chooseAction(EntityLivingBase target) {
        double distanceSq = getDistanceSq(target);
        if (chainCooldown == 0 && target instanceof EntityPlayer && canHarm(target)
                && distanceSq > 16.0D && distanceSq <= CHAIN_RANGE * CHAIN_RANGE
                && hasClearChainShot(target)) {
            return FIRE_CHAIN;
        }
        return super.chooseAction(target);
    }

    @Override
    protected void onActionStarted(byte action, EntityLivingBase target) {
        if (action == FIRE_CHAIN) {
            chainCooldown = CHAIN_COOLDOWN_TICKS;
            chainFacingLocked = false;
            playSound(SoundEvents.ITEM_FIRECHARGE_USE, 1.0F, 0.65F);
        } else if (action == HAMMER_SWING) {
            slashFacingLocked = false;
            playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.1F, 0.6F);
        } else {
            super.onActionStarted(action, target);
        }
    }

    @Override
    protected boolean shouldTrackTargetDuringAction(byte action, int elapsed) {
        return action == FIRE_CHAIN ? elapsed < CHAIN_RELEASE_TICK
                : action == HAMMER_SWING ? elapsed < SLASH_FACING_LOCK_TICK
                : super.shouldTrackTargetDuringAction(action, elapsed);
    }

    @Override
    protected void onActionTick(byte action, int elapsed, EntityLivingBase target) {
        if (action == HAMMER_SWING) {
            if (elapsed >= SLASH_FACING_LOCK_TICK) {
                if (!slashFacingLocked) {
                    slashYaw = rotationYaw;
                    slashFacingLocked = true;
                }
                rotationYaw = renderYawOffset = slashYaw;
            }
        } else if (action != FIRE_CHAIN) {
            super.onActionTick(action, elapsed, target);
        } else if (elapsed >= CHAIN_RELEASE_TICK) {
            if (!chainFacingLocked) {
                chainYaw = rotationYaw;
                chainFacingLocked = true;
            }
            rotationYaw = renderYawOffset = chainYaw;
        }
    }

    @Override
    protected void resolveAction(byte action, EntityLivingBase target) {
        if (action == HAMMER_SWING) {
            resolveSlash();
        } else if (action != FIRE_CHAIN) {
            super.resolveAction(action, target);
        } else if (target instanceof EntityPlayer && target == getAttackTarget()
                && canHarm(target) && getDistanceSq(target) <= CHAIN_RANGE * CHAIN_RANGE
                && hasClearChainShot(target)) {
            EntityFireGiantChain chain = new EntityFireGiantChain(this, (EntityPlayer) target);
            Vec3d hand = getCastingHandPosition(1.0F);
            chain.setPosition(hand.x, hand.y, hand.z);
            Vec3d aim = getChainAimPosition(target).subtract(hand);
            chain.shoot(aim.x, aim.y, aim.z, EntityFireGiantChain.SHOT_SPEED, 0.0F);
            world.spawnEntity(chain);
        }
    }

    private void resolveSlash() {
        if (world.isRemote) { return; }
        playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.8F, 0.5F);
        // Recheck the front half-circle at impact, after facing has already locked.
        for (EntityLivingBase victim : world.getEntitiesWithinAABB(EntityLivingBase.class,
                getEntityBoundingBox().grow(SLASH_REACH))) {
            if (canHarm(victim) && isWithinSlash(victim.posX - posX,
                    victim.posY - posY, victim.posZ - posZ, rotationYaw)
                    && getEntitySenses().canSee(victim) && hurtTarget(victim, 15.0F)) {
                knockAway(victim, 2.0F);
            }
        }
    }

    /** Front 180-degree sector, with true reach and height limits in world units. */
    static boolean isWithinSlash(double dx, double dy, double dz, float yawDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double forward = -Math.sin(yaw) * dx + Math.cos(yaw) * dz;
        return forward >= -1.0E-7D
                && Math.abs(dy) <= SLASH_MAX_HEIGHT_DIFFERENCE
                && dx * dx + dy * dy + dz * dz <= SLASH_REACH * SLASH_REACH;
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote && chainCooldown > 0) { chainCooldown--; }
        super.onLivingUpdate();
    }

    /** World-space left-hand attachment, shared by the projectile and its renderer. */
    public Vec3d getCastingHandPosition(float partialTicks) {
        double x = prevPosX + (posX - prevPosX) * partialTicks;
        double y = prevPosY + (posY - prevPosY) * partialTicks;
        double z = prevPosZ + (posZ - prevPosZ) * partialTicks;
        double yaw = Math.toRadians(prevRenderYawOffset
                + MathHelper.wrapDegrees(renderYawOffset - prevRenderYawOffset) * partialTicks);
        double elapsed = getAction() == FIRE_CHAIN ? getActionElapsed() + partialTicks
                : CHAIN_RELEASE_TICK;
        return new Vec3d(x, y, z).add(chainHandOffset(elapsed, yaw));
    }

    static Vec3d chainHandOffset(double elapsed, double yaw) {
        double time = MathHelper.clamp(elapsed, CHAIN_HAND[0][0],
                CHAIN_HAND[CHAIN_HAND.length - 1][0]);
        int index = 0;
        while (index < CHAIN_HAND.length - 2 && CHAIN_HAND[index + 1][0] < time) { index++; }
        double[] a = CHAIN_HAND[index];
        double[] b = CHAIN_HAND[index + 1];
        double blend = (time - a[0]) / (b[0] - a[0]);
        double handX = (a[1] + (b[1] - a[1]) * blend) * MODEL_UNIT;
        double handY = (a[2] + (b[2] - a[2]) * blend) * MODEL_UNIT;
        double handZ = (a[3] + (b[3] - a[3]) * blend) * MODEL_UNIT;
        // GeoEntityRenderer rotates the native model by 180 - body yaw.
        return new Vec3d(-handX * Math.cos(yaw) + handZ * Math.sin(yaw),
                handY + 0.01D, -handX * Math.sin(yaw) - handZ * Math.cos(yaw));
    }

    public int getChainActionElapsed() { return getActionElapsed(); }

    public static Vec3d getChainAimPosition(EntityLivingBase target) {
        return target.getPositionVector().add(0.0D, target.height * 0.55D, 0.0D);
    }

    private boolean hasClearChainShot(EntityLivingBase target) {
        if (!canHarm(target) || !getEntitySenses().canSee(target)) { return false; }
        Vec3d hand = getCastingHandPosition(1.0F);
        return world.rayTraceBlocks(new Vec3d(posX, hand.y, posZ), hand,
                false, true, false) == null
                && world.rayTraceBlocks(hand, getChainAimPosition(target),
                false, true, false) == null;
    }
}
