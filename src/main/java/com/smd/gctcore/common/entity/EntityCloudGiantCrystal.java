package com.smd.gctcore.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.UUID;

/** A fixed arena crystal whose only vulnerable state is active. */
public class EntityCloudGiantCrystal extends EntityMob implements IAnimatable {

    private static final byte DORMANT = 0;
    private static final byte CHARGING = 1;
    private static final byte ACTIVE = 2;
    private static final byte HURT = 3;
    private static final byte DEPLETED = 4;
    private static final int CHARGING_TICKS = 24;
    private static final int HURT_TICKS = 8;
    private static final int DEPLETED_TICKS = 27;
    private static final String ANIMATION_PREFIX = "animation.cloud_giant_crystal.";
    private static final ResourceLocation[] GUARD_TYPES = {
            new ResourceLocation("aether_legacy", "sentry"),
            new ResourceLocation("aether_legacy", "valkyrie"),
            new ResourceLocation("aether_legacy", "fire_minion")
    };
    private static final DataParameter<Boolean> ACTIVATED = EntityDataManager.createKey(
            EntityCloudGiantCrystal.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Byte> ACTION = EntityDataManager.createKey(
            EntityCloudGiantCrystal.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> ACTION_START = EntityDataManager.createKey(
            EntityCloudGiantCrystal.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ACTION_SEQUENCE = EntityDataManager.createKey(
            EntityCloudGiantCrystal.class, DataSerializers.VARINT);

    private final AnimationFactory animationFactory = new AnimationFactory(this);
    private UUID bossUuid;
    private int displayedActionSequence = Integer.MIN_VALUE;
    private byte displayedAction = -1;

    public EntityCloudGiantCrystal(World world) {
        super(world);
        setSize(0.8F, 1.25F);
        enablePersistence();
        if (!world.isRemote) {
            setHealth(0.0F);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ACTIVATED, false);
        dataManager.register(ACTION, DORMANT);
        dataManager.register(ACTION_START, 0);
        dataManager.register(ACTION_SEQUENCE, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
    }

    @Override
    protected void initEntityAI() {
    }

    @Override
    public boolean isEntityAlive() {
        return !isDead;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (world.isRemote || isDead || !isActivated() || !(source.getTrueSource() instanceof EntityPlayer)
                || source.getImmediateSource() != source.getTrueSource() || amount <= 0.0F) {
            return false;
        }
        float health = Math.max(0.0F, getHealth() - 1.0F);
        setHealth(health);
        if (health <= 0.0F) {
            dataManager.set(ACTIVATED, false);
            setAction(DEPLETED);
            notifyBoss(false);
        } else {
            setAction(HURT);
        }
        return true;
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote) {
            motionX = motionY = motionZ = 0.0D;
            tickAction();
            Entity owner = findLoadedEntity(bossUuid);
            if (owner != null && owner.isDead) {
                setDead();
            }
        }
        super.onLivingUpdate();
    }

    @Override
    protected void onDeathUpdate() {
        deathTime = 0;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        motionX = motionY = motionZ = 0.0D;
    }

    @Override
    public void knockBack(Entity entity, float strength, double xRatio, double zRatio) {
    }

    @Override
    public void applyEntityCollision(Entity entity) {
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    public boolean isActivated() {
        return dataManager.get(ACTIVATED);
    }

    public void setBossUuid(UUID bossUuid) {
        this.bossUuid = bossUuid;
    }

    public UUID getBossUuid() {
        return bossUuid;
    }

    public void activate() {
        if (world.isRemote || isActivated() || isDead) {
            return;
        }
        setHealth(getMaxHealth());
        dataManager.set(ACTIVATED, true);
        setAction(CHARGING);
        notifyBoss(true);
        summonGuards();
    }

    private void summonGuards() {
        if (!Loader.isModLoaded("aether_legacy")) {
            return;
        }
        int[][] positions = {{-8, -8}, {-8, 8}, {8, -8}, {8, 8}};
        for (int[] offset : positions) {
            ResourceLocation type = GUARD_TYPES[rand.nextInt(GUARD_TYPES.length)];
            Entity guard = net.minecraft.entity.EntityList.createEntityByIDFromName(type, world);
            if (guard == null) {
                continue;
            }
            guard.setLocationAndAngles(posX + offset[0], posY, posZ + offset[1], rand.nextFloat() * 360.0F, 0.0F);
            world.getChunk(new BlockPos(guard.posX, guard.posY, guard.posZ));
            world.spawnEntity(guard);
        }
    }

    private void notifyBoss(boolean active) {
        if (bossUuid == null) {
            return;
        }
        Entity owner = findLoadedEntity(bossUuid);
        if (owner instanceof EntityCloudGiantPriest) {
            ((EntityCloudGiantPriest) owner).setCrystalActive(getUniqueID(), active);
        }
    }

    private Entity findLoadedEntity(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        for (Entity entity : world.loadedEntityList) {
            if (uuid.equals(entity.getUniqueID())) {
                return entity;
            }
        }
        return null;
    }

    private void setAction(byte action) {
        dataManager.set(ACTION, action);
        dataManager.set(ACTION_START, (int) world.getTotalWorldTime());
        dataManager.set(ACTION_SEQUENCE, dataManager.get(ACTION_SEQUENCE) + 1);
    }

    private void tickAction() {
        byte action = dataManager.get(ACTION);
        int elapsed = (int) world.getTotalWorldTime() - dataManager.get(ACTION_START);
        if (action == CHARGING && elapsed >= CHARGING_TICKS) {
            setAction(ACTIVE);
        } else if (action == HURT && elapsed >= HURT_TICKS) {
            setAction(ACTIVE);
        } else if (action == DEPLETED && elapsed >= DEPLETED_TICKS) {
            setAction(DORMANT);
        }
    }

    private PlayState animationPredicate(AnimationEvent<EntityCloudGiantCrystal> event) {
        byte action = dataManager.get(ACTION);
        int sequence = dataManager.get(ACTION_SEQUENCE);
        if (sequence != displayedActionSequence || action != displayedAction) {
            event.getController().markNeedsReload();
            displayedActionSequence = sequence;
            displayedAction = action;
        }
        String animation;
        EDefaultLoopTypes loop;
        switch (action) {
            case CHARGING:
                animation = "charging";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case ACTIVE:
                animation = "activated";
                loop = EDefaultLoopTypes.LOOP;
                break;
            case HURT:
                animation = "hurt";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            case DEPLETED:
                animation = "depleted";
                loop = EDefaultLoopTypes.PLAY_ONCE;
                break;
            default:
                return PlayState.STOP;
        }
        event.getController().setAnimation(new AnimationBuilder().addAnimation(ANIMATION_PREFIX + animation, loop));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<EntityCloudGiantCrystal>(
                this, "crystal", 0, this::animationPredicate) {
            @Override
            protected double adjustTick(double tick) {
                double adjustedTick = super.adjustTick(tick);
                if (getAnimationState() == AnimationState.Running && dataManager.get(ACTION) != DORMANT) {
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

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("CrystalActivated", isActivated());
        if (bossUuid != null) {
            compound.setUniqueId("BossUuid", bossUuid);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("BossUuid")) {
            bossUuid = compound.getUniqueId("BossUuid");
        }
        boolean active = compound.getBoolean("CrystalActivated");
        dataManager.set(ACTIVATED, active);
        if (!active) {
            setHealth(0.0F);
            dataManager.set(ACTION, DORMANT);
        } else if (getHealth() <= 0.0F) {
            setHealth(getMaxHealth());
        }
        enablePersistence();
    }
}
