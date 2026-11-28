package com.smd.gctcore.common.entity;

import com.smd.gctcore.misc.GiantLootTables;
import com.smd.gctcore.misc.SoundRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/** A two-headed mini boss: alternating weapons, a long telegraphed slam, and no terrain damage. */
public class EntityEttin extends EntityAnimatedGiant {
    public static final byte AXE_SWEEP = 1;
    public static final byte CLUB_SLAM = 2;
    public static final byte DOUBLE_SLAM = 3;
    public static final byte INTRO_ROAR = 4;
    public static final int AXE_TICKS = 36;
    public static final int AXE_HIT_TICK = 16;
    public static final int CLUB_TICKS = 40;
    public static final int CLUB_HIT_TICK = 20;
    public static final int DOUBLE_TICKS = 56;
    public static final int DOUBLE_HIT_TICK = 30;
    public static final int DOUBLE_COOLDOWN_TICKS = 200;
    public static final int INTRO_TICKS = 36;
    public static final int HURT_TICKS = 9;
    public static final int DEATH_ANIMATION_TICKS = 50;
    public static final int CORPSE_TICKS = 40;
    private static final double AXE_REACH = 5.5D;
    private static final double CLUB_REACH = 5.0D;
    private static final double DOUBLE_REACH = 5.5D;
    private static final DataParameter<Integer> HURT_START = EntityDataManager.createKey(
            EntityEttin.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DEATH_START = EntityDataManager.createKey(
            EntityEttin.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ANIMATION_SEQUENCE = EntityDataManager.createKey(
            EntityEttin.class, DataSerializers.VARINT);

    private int doubleCooldown = DOUBLE_COOLDOWN_TICKS;
    private boolean introPlayed;
    private boolean nextClub;
    private boolean facingLocked;
    private float lockedYaw;
    private boolean deathExperienceAwarded;
    private int displayedSequence = Integer.MIN_VALUE;
    private int displayedState = Integer.MIN_VALUE;

    public EntityEttin(World world) {
        super(world);
        setSize(1.8F, 4.5F);
        experienceValue = 80;
        enablePersistence();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(HURT_START, Integer.MIN_VALUE);
        dataManager.register(DEATH_START, Integer.MIN_VALUE);
        dataManager.register(ANIMATION_SEQUENCE, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(280.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.27D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.8D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
    }

    @Override
    protected double getAcquireRange() { return 24.0D; }

    @Override
    protected String getGiantFaction() { return "ettin"; }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> type) {
        return !EntityEttin.class.isAssignableFrom(type) && super.canAttackClass(type);
    }

    @Override
    protected boolean isValidCombatTarget(EntityLivingBase target) {
        return super.isValidCombatTarget(target) && !isProtectedPlayer(target);
    }

    @Override
    protected double getMeleeReach() { return nextClub ? CLUB_REACH : AXE_REACH; }

    @Override
    protected String getAnimationPrefix() { return "animation.ettin."; }

    @Override
    protected String getActionAnimation(byte action) {
        switch (action) {
            case CLUB_SLAM: return "club_slam";
            case DOUBLE_SLAM: return "double_slam";
            case INTRO_ROAR: return "intro_roar";
            default: return "axe_sweep";
        }
    }

    @Override
    protected int getActionLength(byte action) {
        switch (action) {
            case CLUB_SLAM: return CLUB_TICKS;
            case DOUBLE_SLAM: return DOUBLE_TICKS;
            case INTRO_ROAR: return INTRO_TICKS;
            default: return AXE_TICKS;
        }
    }

    @Override
    protected int getActionImpactTick(byte action) {
        switch (action) {
            case CLUB_SLAM: return CLUB_HIT_TICK;
            case DOUBLE_SLAM: return DOUBLE_HIT_TICK;
            case INTRO_ROAR: return Integer.MAX_VALUE;
            default: return AXE_HIT_TICK;
        }
    }

    @Override
    protected byte chooseAction(EntityLivingBase target) {
        if (isHurtAnimationPlaying() || !canHarm(target) || !getEntitySenses().canSee(target)) {
            return IDLE;
        }
        if (!introPlayed) { return INTRO_ROAR; }
        double distanceSq = getDistanceSq(target);
        if (doubleCooldown == 0 && distanceSq <= DOUBLE_REACH * DOUBLE_REACH) { return DOUBLE_SLAM; }
        double reach = nextClub ? CLUB_REACH : AXE_REACH;
        return distanceSq <= reach * reach ? nextClub ? CLUB_SLAM : AXE_SWEEP : IDLE;
    }

    @Override
    protected void onActionStarted(byte action, EntityLivingBase target) {
        facingLocked = false;
        nextAnimation();
        if (action == INTRO_ROAR) {
            introPlayed = true;
            playSound(SoundRegistry.ETTIN_ROAR, 1.6F, 0.7F);
        } else {
            if (action == DOUBLE_SLAM) { doubleCooldown = DOUBLE_COOLDOWN_TICKS; }
            if (action == AXE_SWEEP) { nextClub = true; }
            if (action == CLUB_SLAM) { nextClub = false; }
            playSound(SoundRegistry.ETTIN_ATTACK, 1.2F, action == AXE_SWEEP ? 0.85F : 0.65F);
        }
    }

    private int getFacingLockTick(byte action) {
        return action == AXE_SWEEP ? 8 : action == CLUB_SLAM ? 10 : action == DOUBLE_SLAM ? 16 : 0;
    }

    @Override
    protected boolean shouldTrackTargetDuringAction(byte action, int elapsed) {
        return elapsed < getFacingLockTick(action);
    }

    @Override
    protected void onActionTick(byte action, int elapsed, EntityLivingBase target) {
        if (elapsed >= getFacingLockTick(action)) {
            if (!facingLocked) { lockedYaw = rotationYaw; facingLocked = true; }
            rotationYaw = renderYawOffset = lockedYaw;
        }
        if (action == DOUBLE_SLAM && elapsed < DOUBLE_HIT_TICK && elapsed % 5 == 0) {
            telegraph(getPositionVector(), DOUBLE_REACH, EnumParticleTypes.CRIT);
        }
    }

    private boolean isEngagedVictim(EntityLivingBase victim, EntityLivingBase target) {
        return victim instanceof EntityPlayer || EntityAIGiantRivalTarget.isOtherGiant(victim)
                || victim == target || victim == getAttackTarget()
                || victim == getRevengeTarget();
    }

    @Override
    protected void resolveAction(byte action, EntityLivingBase target) {
        if (world.isRemote || action == INTRO_ROAR || action == IDLE) { return; }
        double reach = action == CLUB_SLAM ? CLUB_REACH : AXE_REACH;
        float halfAngle = action == CLUB_SLAM ? 55.0F : action == DOUBLE_SLAM ? 90.0F : 90.0F;
        float damage = action == DOUBLE_SLAM ? 20.0F : action == CLUB_SLAM ? 14.0F : 12.0F;
        playSound(action == AXE_SWEEP ? SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP
                : SoundEvents.BLOCK_STONE_FALL, 1.4F, 0.65F);
        for (EntityLivingBase victim : world.getEntitiesWithinAABB(EntityLivingBase.class,
                getEntityBoundingBox().grow(reach))) {
            if (isEngagedVictim(victim, target) && Math.abs(victim.posY - posY) <= 2.5D
                    && canMeleeHit(victim, reach, halfAngle) && hurtTarget(victim, damage)) {
                knockAway(victim, action == DOUBLE_SLAM ? 1.7F : action == AXE_SWEEP ? 0.7F : 0.9F);
            }
        }
    }

    private void nextAnimation() {
        dataManager.set(ANIMATION_SEQUENCE, dataManager.get(ANIMATION_SEQUENCE) + 1);
    }

    private int getHurtElapsed() {
        int start = dataManager.get(HURT_START);
        return start == Integer.MIN_VALUE ? HURT_TICKS : (int) world.getTotalWorldTime() - start;
    }

    public boolean isHurtAnimationPlaying() {
        return isEntityAlive() && getHurtElapsed() >= 0 && getHurtElapsed() < HURT_TICKS;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean wasIdle = isEntityAlive() && !isPerformingAction() && !isHurtAnimationPlaying();
        boolean accepted = super.attackEntityFrom(source, amount);
        if (accepted && wasIdle && isEntityAlive() && !world.isRemote) {
            dataManager.set(HURT_START, (int) world.getTotalWorldTime());
            nextAnimation();
            stopWalking();
        }
        return accepted;
    }

    @Override
    protected void updateCombatMovement(EntityLivingBase target) {
        if (isHurtAnimationPlaying()) { stopWalking(); }
        else { super.updateCombatMovement(target); }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (isHurtAnimationPlaying()) {
            strafe = forward = 0.0F;
            motionX = motionZ = 0.0D;
        }
        super.travel(strafe, vertical, forward);
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote) {
            if (isEntityAlive() && isValidCombatTarget(getAttackTarget()) && doubleCooldown > 0) {
                doubleCooldown--;
            }
        }
        super.onLivingUpdate();
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (!world.isRemote && dead && dataManager.get(DEATH_START) == Integer.MIN_VALUE) {
            dataManager.set(DEATH_START, (int) world.getTotalWorldTime());
            nextAnimation();
            stopWalking();
        }
    }

    @Override
    protected void onDeathUpdate() {
        deathTime++;
        motionX = motionZ = 0.0D;
        // Preserve Forge's normal XP event and vanilla doMobLoot/player credit rules.
        if (!world.isRemote && deathTime >= 20 && !deathExperienceAwarded) {
            deathExperienceAwarded = true;
            if (recentlyHit > 0 && canDropLoot() && world.getGameRules().getBoolean("doMobLoot")) {
                int experience = ForgeEventFactory.getExperienceDrop(this, attackingPlayer,
                        getExperiencePoints(attackingPlayer));
                while (experience > 0) {
                    int split = EntityXPOrb.getXPSplit(experience);
                    experience -= split;
                    world.spawnEntity(new EntityXPOrb(world, posX, posY, posZ, split));
                }
            }
        }
        if (!world.isRemote && deathTime >= DEATH_ANIMATION_TICKS + CORPSE_TICKS) { setDead(); }
    }

    private int getDeathElapsed() {
        int start = dataManager.get(DEATH_START);
        return start == Integer.MIN_VALUE ? deathTime
                : Math.max(deathTime, (int) world.getTotalWorldTime() - start);
    }

    @Override
    protected boolean canDespawn() { return false; }

    @Override
    public boolean isNonBoss() { return false; }

    @Override
    protected ResourceLocation getLootTable() { return GiantLootTables.ETTIN; }

    @Override
    protected SoundEvent getAmbientSound() { return SoundRegistry.ETTIN_AMBIENT; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundRegistry.ETTIN_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundRegistry.ETTIN_DEATH; }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("EttinIntroPlayed", introPlayed);
        tag.setBoolean("EttinNextClub", nextClub);
        tag.setInteger("EttinDoubleCooldown", doubleCooldown);
        tag.setBoolean("EttinDeathExperienceAwarded", deathExperienceAwarded);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        introPlayed = tag.getBoolean("EttinIntroPlayed");
        nextClub = tag.getBoolean("EttinNextClub");
        doubleCooldown = tag.hasKey("EttinDoubleCooldown")
                ? Math.max(0, Math.min(DOUBLE_COOLDOWN_TICKS, tag.getInteger("EttinDoubleCooldown")))
                : DOUBLE_COOLDOWN_TICKS;
        deathExperienceAwarded = tag.getBoolean("EttinDeathExperienceAwarded");
        if (getHealth() <= 0.0F) {
            dataManager.set(DEATH_START, (int) world.getTotalWorldTime() - deathTime);
        }
    }

    private PlayState animationPredicate(AnimationEvent<EntityEttin> event) {
        int state = !isEntityAlive() ? 100 : isPerformingAction() ? getAction()
                : isHurtAnimationPlaying() ? 99 : IDLE;
        int sequence = dataManager.get(ANIMATION_SEQUENCE);
        if (state != displayedState || sequence != displayedSequence) {
            event.getController().markNeedsReload();
            displayedState = state;
            displayedSequence = sequence;
        }
        String clip = state == 100 ? "death" : state == 99 ? "hurt"
                : isPerformingAction() ? getActionAnimation(getAction()) : event.isMoving() ? "walk" : "idle";
        event.getController().setAnimation(new AnimationBuilder().addAnimation(getAnimationPrefix() + clip,
                state == 100 ? EDefaultLoopTypes.HOLD_ON_LAST_FRAME
                        : state == IDLE ? EDefaultLoopTypes.LOOP : EDefaultLoopTypes.PLAY_ONCE));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<EntityEttin>(this, "body", 0, this::animationPredicate) {
            @Override
            protected double adjustTick(double tick) {
                double adjusted = super.adjustTick(tick);
                if (getAnimationState() == AnimationState.Running) {
                    double partial = tick - Math.floor(tick);
                    // GeckoLib 3.0.31 still stops HOLD_ON_LAST_FRAME at the endpoint.
                    if (!isEntityAlive()) {
                        return Math.min(Math.max(0, getDeathElapsed()) + partial,
                                Math.nextDown((double) DEATH_ANIMATION_TICKS));
                    }
                    if (isPerformingAction()) { return Math.max(0, getActionElapsed()) + partial; }
                    if (isHurtAnimationPlaying()) { return Math.max(0, getHurtElapsed()) + partial; }
                }
                return adjusted;
            }
        });
    }
}
