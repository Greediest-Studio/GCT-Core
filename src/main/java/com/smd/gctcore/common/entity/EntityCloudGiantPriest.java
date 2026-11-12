package com.smd.gctcore.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.Loader;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/** Server-authoritative combat; the authored Blockbench animations are played on clients. */
public class EntityCloudGiantPriest extends EntityMob implements IAnimatable {

    public static final String BOSS_TITLE_SUFFIX = ", 云巨人祭司";
    public static final int SWEEP_TICKS = 34;
    public static final int SWEEP_HIT_TICK = SWEEP_TICKS / 2;
    public static final int SLAM_TICKS = 32;
    public static final int SLAM_HIT_TICK = 16;
    public static final int STORM_CAST_TICKS = 64;
    public static final int JUMP_TICKS = 13;
    public static final int THRUST_TICKS = 14;
    public static final int WHIRLWIND_CAST_TICKS = 40;
    public static final int PHASE_TRANSITION_TICKS = 60;
    public static final int INTRO_TICKS = 60;
    public static final int HURT_TICKS = 11;
    public static final int DEATH_ANIMATION_TICKS = 64;
    public static final int CORPSE_TICKS = 60;
    public static final int SLAM_COOLDOWN_TICKS = 120;
    public static final int LIGHTNING_COOLDOWN_TICKS = 100;
    public static final int CLOUD_LEAP_COOLDOWN_TICKS = 200;
    public static final int WHIRLWIND_COOLDOWN_TICKS = 320;
    private static final double WHIRLWIND_SPAWN_RADIUS = 1.25D;

    private static final String[] NAMES = {
            "Skýjötunn", "Hrafnský", "Þokujötunn", "Himinþurs",
            "Drífandi", "Skýrvandr", "Vindský", "Himinrisi",
            "Vetrhimin", "Skýgandr", "Hrímvegr", "Þrumjötunn",
            "Nimbulþurs", "Himinbrjótr", "Stormrisi", "Móðuský"
    };
    private static final String ANIMATION_PREFIX = "animation.cloud_giant_wanderer.";
    private static final ResourceLocation AETHER_WHIRLWIND =
            new ResourceLocation("aether_legacy", "whirlwind");
    private static final byte IDLE = 0;
    private static final byte SWEEP = 1;
    private static final byte SLAM = 2;
    private static final byte STORM_CAST = 3;
    private static final byte DEATH = 4;
    private static final byte INTRO = 5;
    private static final byte HURT = 6;
    private static final byte PHASE_TRANSITION = 7;
    private static final byte JUMP = 8;
    private static final byte FORWARD_THRUST = 9;
    private static final byte WHIRLWIND_CAST = 10;
    private static final DataParameter<String> PRIEST_NAME = EntityDataManager.createKey(
            EntityCloudGiantPriest.class, DataSerializers.STRING);
    private static final DataParameter<Byte> ACTION = EntityDataManager.createKey(
            EntityCloudGiantPriest.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> ACTION_SEQUENCE = EntityDataManager.createKey(
            EntityCloudGiantPriest.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ACTION_START = EntityDataManager.createKey(
            EntityCloudGiantPriest.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SECOND_PHASE = EntityDataManager.createKey(
            EntityCloudGiantPriest.class, DataSerializers.BOOLEAN);

    private final AnimationFactory animationFactory = new AnimationFactory(this);
    private final BossInfoServer bossInfo = new BossInfoServer(
            getDisplayName(), BossInfo.Color.WHITE, BossInfo.Overlay.PROGRESS);
    private EntityLivingBase actionTarget;
    private int actionTicks;
    private int slamCooldown;
    private int lightningCooldown;
    private int cloudLeapCooldown;
    private int whirlwindCooldown;
    private int lastRevengeTimer;
    private boolean introPlayed;
    private boolean thrustHitResolved;
    private final Set<UUID> summonedWhirlwinds = new HashSet<>();
    private int displayedActionSequence = Integer.MIN_VALUE;
    private byte displayedAction = -1;

    public EntityCloudGiantPriest(World world) {
        super(world);
        setSize(1.0F, 4.0F);
        stepHeight = 1.0F;
        experienceValue = 100;
        enablePersistence();
        bossInfo.setDarkenSky(false);
        if (!world.isRemote) {
            dataManager.set(PRIEST_NAME, NAMES[rand.nextInt(NAMES.length)]);
            bossInfo.setName(getDisplayName());
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(PRIEST_NAME, NAMES[0]);
        dataManager.register(ACTION, IDLE);
        dataManager.register(ACTION_SEQUENCE, 0);
        dataManager.register(ACTION_START, 0);
        dataManager.register(SECOND_PHASE, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(500.0D);
        // The vanilla spider's base movement speed.
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(15.0D);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new CombatTask());
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.8D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        // Vanilla target goals discard creative players and targets beyond follow range.
        // Acquisition and retention are handled below so a flying aggro target stays valid.
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote && isEntityAlive()) {
            if (!introPlayed) {
                introPlayed = true;
                beginAction(INTRO, null);
            }
            activateSecondPhaseIfThresholdReached();
            if (!isAIDisabled()) {
                if (slamCooldown > 0) --slamCooldown;
                if (lightningCooldown > 0) --lightningCooldown;
                if (cloudLeapCooldown > 0) --cloudLeapCooldown;
                if (whirlwindCooldown > 0) --whirlwindCooldown;
                updateCombatTarget();
            }
            // Finish actions before goals run so their damage clocks match the authored clips.
            tickAction();
            healPlayersCaughtInWhirlwinds();
        }
        super.onLivingUpdate();
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        byte action = getAction();
        boolean lunging = action == JUMP || action == FORWARD_THRUST;
        if ((!lunging && action != IDLE) || !isEntityAlive()) {
            strafe = 0.0F;
            forward = 0.0F;
            motionX = 0.0D;
            motionZ = 0.0D;
        }
        super.travel(strafe, vertical, forward);
    }

    private void updateCombatTarget() {
        EntityLivingBase revengeTarget = getRevengeTarget();
        if (getRevengeTimer() != lastRevengeTimer && isValidCombatTarget(revengeTarget)) {
            lastRevengeTimer = getRevengeTimer();
            setAttackTarget(revengeTarget);
        }
        if (isValidCombatTarget(getAttackTarget())) {
            return;
        }
        setAttackTarget(null);
        if (ticksExisted % 10 != 0) {
            return;
        }
        double nearestDistance = getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue();
        nearestDistance *= nearestDistance;
        EntityPlayer nearest = null;
        for (EntityPlayer player : world.playerEntities) {
            if (!isValidCombatTarget(player)
                    || player.capabilities.disableDamage && !(player.isCreative() && player.capabilities.isFlying)
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

    private boolean isValidCombatTarget(EntityLivingBase target) {
        return target != null && target != this && target.world == world && target.isEntityAlive()
                && !isOnSameTeam(target)
                && (!(target instanceof EntityPlayer) || !((EntityPlayer) target).isSpectator());
    }

    private void activateSecondPhaseIfThresholdReached() {
        if (!isEntityAlive() || dataManager.get(SECOND_PHASE) || getHealth() > getMaxHealth() * 0.5F) {
            return;
        }
        dataManager.set(SECOND_PHASE, true);
        beginAction(PHASE_TRANSITION, null);
    }

    public boolean isInSecondPhase() {
        return dataManager.get(SECOND_PHASE);
    }

    private byte getAction() {
        return dataManager.get(ACTION);
    }

    private void beginAction(byte action, EntityLivingBase target) {
        actionTarget = target;
        actionTicks = 0;
        if (action == FORWARD_THRUST) {
            thrustHitResolved = false;
        }
        dataManager.set(ACTION_START, (int) world.getTotalWorldTime());
        dataManager.set(ACTION, action);
        dataManager.set(ACTION_SEQUENCE, dataManager.get(ACTION_SEQUENCE) + 1);
        getNavigator().clearPath();
        setMoveForward(0.0F);
        setMoveStrafing(0.0F);
    }

    private void tickAction() {
        byte action = getAction();
        if (action == IDLE || action == DEATH) {
            return;
        }
        if (action == JUMP && isValidCombatTarget(actionTarget)) {
            chargeToward(actionTarget, 0.72D);
        } else if (action == FORWARD_THRUST && isValidCombatTarget(actionTarget)) {
            chargeToward(actionTarget, 1.25D);
            if (!thrustHitResolved && intersectsThrustPath(actionTarget)) {
                thrustHitResolved = true;
                damageTarget(actionTarget, 25.0F, false);
            }
        }
        ++actionTicks;
        int duration = getActionDuration(action);
        if (action == SWEEP && actionTicks == SWEEP_HIT_TICK
                && isValidCombatTarget(actionTarget) && getDistanceSq(actionTarget) <= 25.0D
                && getEntitySenses().canSee(actionTarget)) {
            damageTarget(actionTarget, 12.0F + rand.nextInt(7), false);
        } else if (action == SLAM && actionTicks == SLAM_HIT_TICK) {
            resolveSlam(actionTarget);
        }
        if (actionTicks >= duration) {
            if (action == JUMP && isValidCombatTarget(actionTarget)) {
                beginAction(FORWARD_THRUST, actionTarget);
                return;
            } else if (action == STORM_CAST && isValidCombatTarget(actionTarget)) {
                // effectOnly prevents lightning damage, fire and mob transformations.
                world.addWeatherEffect(new EntityLightningBolt(world,
                        actionTarget.posX, actionTarget.posY, actionTarget.posZ, true));
                damageTarget(actionTarget, 15.0F, false);
            } else if (action == WHIRLWIND_CAST) {
                summonWhirlwinds(actionTarget);
            }
            if (action == FORWARD_THRUST) {
                motionX = 0.0D;
                motionZ = 0.0D;
            }
            actionTarget = null;
            dataManager.set(ACTION, IDLE);
        }
    }

    private int getActionDuration(byte action) {
        switch (action) {
            case INTRO:
                return INTRO_TICKS;
            case HURT:
                return HURT_TICKS;
            case PHASE_TRANSITION:
                return PHASE_TRANSITION_TICKS;
            case JUMP:
                return JUMP_TICKS;
            case FORWARD_THRUST:
                return THRUST_TICKS;
            case WHIRLWIND_CAST:
                return WHIRLWIND_CAST_TICKS;
            case SWEEP:
                return SWEEP_TICKS;
            case SLAM:
                return SLAM_TICKS;
            default:
                return STORM_CAST_TICKS;
        }
    }

    private void chargeToward(EntityLivingBase target, double speed) {
        double dx = target.posX - posX;
        double dz = target.posZ - posZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance > 0.001D) {
            motionX = dx / distance * speed;
            motionZ = dz / distance * speed;
        }
    }

    private boolean intersectsThrustPath(EntityLivingBase target) {
        double dx = motionX;
        double dz = motionZ;
        double lengthSq = dx * dx + dz * dz;
        double projection = lengthSq > 0.0001D
                ? ((target.posX - posX) * dx + (target.posZ - posZ) * dz) / lengthSq
                : 0.0D;
        projection = Math.max(0.0D, Math.min(1.0D, projection));
        double nearestX = posX + dx * projection;
        double nearestZ = posZ + dz * projection;
        double hitRadius = (width + target.width) * 0.5D + 0.2D;
        if (Math.pow(target.posX - nearestX, 2.0D) + Math.pow(target.posZ - nearestZ, 2.0D)
                > hitRadius * hitRadius) {
            return false;
        }
        double nextY = posY + motionY;
        double thrustMinY = Math.min(posY, nextY);
        double thrustMaxY = Math.max(posY, nextY) + height;
        return target.posY + target.height > thrustMinY && target.posY < thrustMaxY;
    }

    private void performCloudLeap(EntityLivingBase target) {
        beginAction(JUMP, target);
        cloudLeapCooldown = CLOUD_LEAP_COOLDOWN_TICKS;
        motionY = Math.max(motionY, 0.62D);
        isAirBorne = true;
    }

    private void beginWhirlwindCast(EntityLivingBase target) {
        beginAction(WHIRLWIND_CAST, target);
        whirlwindCooldown = WHIRLWIND_COOLDOWN_TICKS;
        playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.2F, 0.55F);
    }

    private void performSlam(EntityLivingBase target) {
        beginAction(SLAM, target);
        slamCooldown = SLAM_COOLDOWN_TICKS;
        playSound(SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1.8F, 0.55F);
    }

    private void resolveSlam(EntityLivingBase target) {
        for (EntityLivingBase victim : world.getEntitiesWithinAABB(EntityLivingBase.class,
                getEntityBoundingBox().grow(7.0D))) {
            if (!isValidCombatTarget(victim) || getDistanceSq(victim) > 49.0D) {
                continue;
            }
            damageTarget(victim, 22.0F, false);
            if (victim instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) victim;
                double dx = player.posX - posX;
                double dz = player.posZ - posZ;
                double length = Math.sqrt(dx * dx + dz * dz);
                if (length < 0.0001D) {
                    dx = -MathHelper.sin(rotationYaw * 0.017453292F);
                    dz = MathHelper.cos(rotationYaw * 0.017453292F);
                    length = 1.0D;
                }
                player.motionX = dx / length * 2.4D;
                player.motionZ = dz / length * 2.4D;
                player.motionY = Math.max(player.motionY, 0.6D);
                groundCreativeFlier(player);
                syncPlayerVelocity(player);
            }
        }
        // The anti-air condition has no distance limit for the current aggro target.
        if (target instanceof EntityPlayer && getDistanceSq(target) > 49.0D) {
            EntityPlayer player = (EntityPlayer) target;
            if (groundCreativeFlier(player)) {
                syncPlayerVelocity(player);
            }
        }
    }

    private void summonWhirlwinds(EntityLivingBase target) {
        if (!Loader.isModLoaded("aether_legacy") || !isValidCombatTarget(target)) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            Entity whirlwind = EntityList.createEntityByIDFromName(AETHER_WHIRLWIND, world);
            if (whirlwind == null) {
                continue;
            }
            double angle = Math.PI * 2.0D * i / 3.0D;
            double x = target.posX + Math.cos(angle) * WHIRLWIND_SPAWN_RADIUS;
            double z = target.posZ + Math.sin(angle) * WHIRLWIND_SPAWN_RADIUS;
            whirlwind.setLocationAndAngles(x, target.posY, z, rand.nextFloat() * 360.0F, 0.0F);
            if (world.spawnEntity(whirlwind)) {
                summonedWhirlwinds.add(whirlwind.getUniqueID());
            }
        }
    }

    private void healPlayersCaughtInWhirlwinds() {
        if (!isInSecondPhase() || ticksExisted % 20 != 0 || summonedWhirlwinds.isEmpty()) {
            return;
        }
        for (EntityPlayer player : world.playerEntities) {
            if (!player.isEntityAlive() || player.getHealth() >= player.getMaxHealth()) {
                continue;
            }
            for (Entity entity : world.loadedEntityList) {
                if (!summonedWhirlwinds.contains(entity.getUniqueID()) || entity.isDead) {
                    continue;
                }
                if (isPlayerCaughtByWhirlwind(player, entity)) {
                    player.heal(player.getMaxHealth() * 0.005F);
                    break;
                }
            }
        }
    }

    private boolean isPlayerCaughtByWhirlwind(EntityPlayer player, Entity whirlwind) {
        AxisAlignedBB influenceBounds = whirlwind.getEntityBoundingBox().grow(2.5D, 2.5D, 2.5D);
        if (!influenceBounds.intersects(player.getEntityBoundingBox())) {
            return false;
        }
        double verticalOffset = (float) player.posY - player.getYOffset() * 0.6F - whirlwind.posY;
        return whirlwind.getDistance(player) <= 1.5D + verticalOffset;
    }

    private boolean groundCreativeFlier(EntityPlayer player) {
        if (!player.isCreative() || !player.capabilities.isFlying) {
            return false;
        }
        player.capabilities.isFlying = false;
        player.sendPlayerAbilities();
        damageTarget(player, 5.0F, true);
        player.motionY = -1.0D;
        player.fallDistance = 0.0F;
        return true;
    }

    private void syncPlayerVelocity(EntityPlayer player) {
        player.velocityChanged = true;
        player.isAirBorne = true;
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).connection.sendPacket(new SPacketEntityVelocity(player));
        }
    }

    private boolean damageTarget(EntityLivingBase target, float amount, boolean creativeFlightDamage) {
        if (target instanceof EntityPlayer && ((EntityPlayer) target).isCreative()) {
            return false;
        }
        // Exact skill damage is not multiplied by Minecraft's difficulty multiplier.
        DamageSource source = new EntityDamageSource("mob", this) {
            @Override
            public boolean isDifficultyScaled() {
                return false;
            }
        };
        if (creativeFlightDamage) {
            source.setDamageAllowedInCreativeMode();
        }
        boolean hit = target.attackEntityFrom(source, amount);
        if (hit) {
            setLastAttackedEntity(target);
        }
        return hit;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (world.isRemote || getAction() != IDLE || !(target instanceof EntityLivingBase)
                || !isValidCombatTarget((EntityLivingBase) target) || getDistanceSq(target) > 25.0D) {
            return false;
        }
        beginAction(SWEEP, (EntityLivingBase) target);
        playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.5F, 0.6F);
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (getAction() == PHASE_TRANSITION) {
            return false;
        }
        if (source.getTrueSource() == null && source.getImmediateSource() == null) {
            return false;
        }
        float previousHealth = getHealth();
        boolean accepted = super.attackEntityFrom(source, Math.min(amount, getMaxHealth() * 0.04F));
        if (accepted && isEntityAlive() && getHealth() < previousHealth
                && getAction() == IDLE) {
            beginAction(HURT, null);
        }
        return accepted;
    }

    @Override
    protected void damageEntity(DamageSource source, float amount) {
        if (getAction() == PHASE_TRANSITION || isEntityInvulnerable(source)) {
            return;
        }
        // Preserve Forge's damage hooks, armor, resistance and absorption, then enforce
        // the health-loss cap again in case another mod amplifies the damage in a hook.
        amount = ForgeHooks.onLivingHurt(this, source, amount);
        if (amount <= 0.0F) {
            return;
        }
        amount = applyArmorCalculations(source, amount);
        amount = applyPotionDamageCalculations(source, amount);
        float beforeAbsorption = amount;
        amount = Math.max(amount - getAbsorptionAmount(), 0.0F);
        setAbsorptionAmount(getAbsorptionAmount() - (beforeAbsorption - amount));
        amount = ForgeHooks.onLivingDamage(this, source, amount);
        amount = MathHelper.clamp(amount, 0.0F, getMaxHealth() * 0.04F);
        if (amount > 0.0F) {
            float health = getHealth();
            getCombatTracker().trackDamage(source, health, amount);
            setHealth(health - amount);
            setAbsorptionAmount(getAbsorptionAmount() - amount);
            activateSecondPhaseIfThresholdReached();
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            bossInfo.setName(getDisplayName());
            bossInfo.setPercent(MathHelper.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);
        if (dead && !world.isRemote && getAction() != DEATH) {
            beginAction(DEATH, null);
            setAttackTarget(null);
            bossInfo.setVisible(false);
        }
    }

    @Override
    protected void onDeathUpdate() {
        ++deathTime;
        motionX = 0.0D;
        motionZ = 0.0D;
        // Award experience once at the vanilla time; corpse removal uses the animation time.
        if (deathTime == 20 && !world.isRemote && recentlyHit > 0 && canDropLoot()
                && world.getGameRules().getBoolean("doMobLoot")) {
            int experience = ForgeEventFactory.getExperienceDrop(this, attackingPlayer,
                    getExperiencePoints(attackingPlayer));
            while (experience > 0) {
                int split = EntityXPOrb.getXPSplit(experience);
                experience -= split;
                world.spawnEntity(new EntityXPOrb(world, posX, posY, posZ, split));
            }
        }
        if (deathTime >= DEATH_ANIMATION_TICKS + CORPSE_TICKS && !world.isRemote) {
            setDead();
        }
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public String getName() {
        return dataManager.get(PRIEST_NAME) + BOSS_TITLE_SUFFIX;
    }

    @Override
    public float getEyeHeight() {
        return 3.45F;
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        bossInfo.addPlayer(player);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        bossInfo.removePlayer(player);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setString("BossName", dataManager.get(PRIEST_NAME));
        compound.setInteger("SlamCooldown", slamCooldown);
        compound.setInteger("LightningCooldown", lightningCooldown);
        compound.setInteger("CloudLeapCooldown", cloudLeapCooldown);
        compound.setInteger("WhirlwindCooldown", whirlwindCooldown);
        compound.setBoolean("IntroPlayed", introPlayed);
        compound.setBoolean("SecondPhase", isInSecondPhase());
        NBTTagList whirlwindIds = new NBTTagList();
        for (UUID uuid : summonedWhirlwinds) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setUniqueId("Id", uuid);
            whirlwindIds.appendTag(entry);
        }
        compound.setTag("SummonedWhirlwinds", whirlwindIds);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("BossName", 8) && !compound.getString("BossName").isEmpty()) {
            dataManager.set(PRIEST_NAME, compound.getString("BossName"));
        }
        slamCooldown = Math.max(0, compound.getInteger("SlamCooldown"));
        lightningCooldown = Math.max(0, compound.getInteger("LightningCooldown"));
        cloudLeapCooldown = Math.max(0, compound.getInteger("CloudLeapCooldown"));
        whirlwindCooldown = Math.max(0, compound.getInteger("WhirlwindCooldown"));
        introPlayed = compound.getBoolean("IntroPlayed");
        dataManager.set(SECOND_PHASE, compound.getBoolean("SecondPhase")
                || getHealth() <= getMaxHealth() * 0.5F);
        summonedWhirlwinds.clear();
        NBTTagList whirlwindIds = compound.getTagList("SummonedWhirlwinds", 10);
        for (int i = 0; i < whirlwindIds.tagCount(); i++) {
            NBTTagCompound entry = whirlwindIds.getCompoundTagAt(i);
            if (entry.hasUniqueId("Id")) {
                summonedWhirlwinds.add(entry.getUniqueId("Id"));
            }
        }
        enablePersistence();
        if (getHealth() <= 0.0F) {
            dataManager.set(ACTION, DEATH);
            dataManager.set(ACTION_START, (int) world.getTotalWorldTime() - deathTime);
            bossInfo.setVisible(false);
        }
        bossInfo.setName(getDisplayName());
        bossInfo.setPercent(MathHelper.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_IRONGOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRONGOLEM_DEATH;
    }

    private PlayState animationPredicate(AnimationEvent<EntityCloudGiantPriest> event) {
        byte action = isEntityAlive() ? getAction() : DEATH;
        int sequence = dataManager.get(ACTION_SEQUENCE);
        if (sequence != displayedActionSequence || action != displayedAction) {
            event.getController().markNeedsReload();
            displayedActionSequence = sequence;
            displayedAction = action;
        }
        String animation;
        EDefaultLoopTypes loop;
        switch (action) {
            case SWEEP:
                animation = "sweep";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case SLAM:
                animation = "slam";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case STORM_CAST:
                animation = "storm_cast";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case INTRO:
                animation = "intro";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case HURT:
                animation = "hurt";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case PHASE_TRANSITION:
                animation = "phase_transition";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case JUMP:
                animation = "jump";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case FORWARD_THRUST:
                animation = "forward_thrust";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case WHIRLWIND_CAST:
                animation = "whirlwind_cast";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case DEATH:
                animation = "death";
                loop = EDefaultLoopTypes.HOLD_ON_LAST_FRAME;
                break;
            default:
                animation = event.isMoving() ? "walk" : "idle";
                loop = EDefaultLoopTypes.LOOP;
        }
        event.getController().setAnimation(new AnimationBuilder().addAnimation(ANIMATION_PREFIX + animation, loop));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<EntityCloudGiantPriest>(
                this, "body", 0, this::animationPredicate) {
            @Override
            protected double adjustTick(double tick) {
                double adjustedTick = super.adjustTick(tick);
                if (getAnimationState() == AnimationState.Running && getAction() != IDLE) {
                    // Joining observers seek to the server's action time, including a held corpse.
                    int elapsed = (int) world.getTotalWorldTime() - dataManager.get(ACTION_START);
                    double actionTime = Math.max(0, elapsed) + tick - Math.floor(tick);
                    // GeckoLib 3.0.31 declares HOLD_ON_LAST_FRAME but still stops at
                    // animationLength. Keep sampling just before the death endpoint.
                    return getAction() == DEATH
                            ? Math.min(actionTime, Math.nextDown((double) DEATH_ANIMATION_TICKS))
                            : actionTime;
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
            return isEntityAlive() && (getAction() != IDLE || isValidCombatTarget(getAttackTarget()));
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = getAttackTarget();
            if (isValidCombatTarget(target)) {
                getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
                faceEntity(target, 20.0F, 20.0F);
            }
            if (getAction() != IDLE) {
                getNavigator().clearPath();
                setMoveForward(0.0F);
                setMoveStrafing(0.0F);
                return;
            }
            if (!isValidCombatTarget(target)) {
                return;
            }
            double distanceSq = getDistanceSq(target);
            boolean airborne = !target.onGround && !target.isInWater() && !target.isInLava();
            if (slamCooldown == 0 && (airborne || distanceSq < 49.0D)) {
                performSlam(target);
            } else if (isInSecondPhase() && distanceSq > 256.0D
                    && cloudLeapCooldown == 0 && rand.nextInt(100) < 15) {
                performCloudLeap(target);
            } else if (isInSecondPhase() && distanceSq > 49.0D && distanceSq <= 256.0D
                    && whirlwindCooldown == 0) {
                beginWhirlwindCast(target);
            } else if (lightningCooldown == 0 && distanceSq >= 100.0D) {
                lightningCooldown = LIGHTNING_COOLDOWN_TICKS;
                beginAction(STORM_CAST, target);
            } else if (distanceSq <= 25.0D) {
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
