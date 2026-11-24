package com.smd.gctcore.common.entity;

import com.smd.gctcore.misc.PotionsItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;

import javax.annotation.Nullable;

/** A fixed-direction hook: only physically hitting the intended player establishes a tether. */
public class EntityFireGiantChain extends Entity implements IProjectile {
    public static final float SHOT_SPEED = 0.85F;
    private static final double STOP_DISTANCE = 2.5D;
    private static final int MAX_LIFETIME = 28;
    private static final DataParameter<Integer> OWNER = EntityDataManager.createKey(
            EntityFireGiantChain.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TARGET = EntityDataManager.createKey(
            EntityFireGiantChain.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> LATCHED = EntityDataManager.createKey(
            EntityFireGiantChain.class, DataSerializers.BOOLEAN);
    private int age;
    private int pullTicks;

    public EntityFireGiantChain(World world) {
        super(world);
        setSize(0.25F, 0.25F);
        setNoGravity(true);
        isImmuneToFire = true;
        ignoreFrustumCheck = true;
    }

    public EntityFireGiantChain(EntityFireGiant owner, EntityPlayer target) {
        this(owner.world);
        dataManager.set(OWNER, owner.getEntityId());
        dataManager.set(TARGET, target.getEntityId());
    }

    @Override
    protected void entityInit() {
        dataManager.register(OWNER, -1);
        dataManager.register(TARGET, -1);
        dataManager.register(LATCHED, false);
    }

    public int getOwnerEntityId() { return dataManager.get(OWNER); }
    public int getTargetEntityId() { return dataManager.get(TARGET); }
    public boolean isLatched() { return dataManager.get(LATCHED); }

    @Nullable
    public EntityFireGiant getOwner() {
        Entity entity = world.getEntityByID(getOwnerEntityId());
        return entity instanceof EntityFireGiant ? (EntityFireGiant) entity : null;
    }

    @Nullable
    public EntityPlayer getTarget() {
        Entity entity = world.getEntityByID(getTargetEntityId());
        return entity instanceof EntityPlayer ? (EntityPlayer) entity : null;
    }

    public Vec3d getEndpoint(float partialTicks) {
        EntityPlayer target = getTarget();
        if (isLatched() && target != null) {
            return new Vec3d(target.prevPosX + (target.posX - target.prevPosX) * partialTicks,
                    target.prevPosY + (target.posY - target.prevPosY) * partialTicks + target.height * 0.55D,
                    target.prevPosZ + (target.posZ - target.prevPosZ) * partialTicks);
        }
        return new Vec3d(prevPosX + (posX - prevPosX) * partialTicks,
                prevPosY + (posY - prevPosY) * partialTicks,
                prevPosZ + (posZ - prevPosZ) * partialTicks);
    }

    public Vec3d getCastingHandPosition(float partialTicks) {
        EntityFireGiant owner = getOwner();
        return owner == null ? getEndpoint(partialTicks) : owner.getCastingHandPosition(partialTicks);
    }

    @Override
    public void shoot(double x, double y, double z, float speed, float inaccuracy) {
        Vec3d direction = new Vec3d(x, y, z).normalize();
        motionX = (direction.x + rand.nextGaussian() * 0.0075D * inaccuracy) * speed;
        motionY = (direction.y + rand.nextGaussian() * 0.0075D * inaccuracy) * speed;
        motionZ = (direction.z + rand.nextGaussian() * 0.0075D * inaccuracy) * speed;
        rotationYaw = (float) Math.toDegrees(Math.atan2(motionX, motionZ));
        rotationPitch = (float) Math.toDegrees(Math.atan2(motionY,
                Math.sqrt(motionX * motionX + motionZ * motionZ)));
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
        velocityChanged = true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (isDead) { return; }
        if (world.isRemote) {
            Vec3d end = isLatched() ? getEndpoint(1.0F)
                    : getPositionVector().add(motionX, motionY, motionZ);
            setPosition(end.x, end.y, end.z);
            world.spawnParticle(EnumParticleTypes.FLAME, posX, posY, posZ, 0.0D, 0.0D, 0.0D);
            return;
        }
        EntityFireGiant owner = getOwner();
        EntityPlayer target = getTarget();
        if (++age > MAX_LIFETIME || owner == null || !owner.isEntityAlive()
                || owner.getAction() != EntityFireGiant.FIRE_CHAIN
                || owner.getChainActionElapsed() >= EntityFireGiant.CHAIN_PULL_END_TICK
                || target == null || !owner.canHarm(target) || owner.getAttackTarget() != target
                || owner.getDistanceSq(target) > EntityFireGiant.CHAIN_RANGE * EntityFireGiant.CHAIN_RANGE
                || isImmobilized(target)) {
            setDead();
            return;
        }
        Vec3d hand = owner.getCastingHandPosition(1.0F);
        Vec3d end = isLatched() ? EntityFireGiant.getChainAimPosition(target)
                : getPositionVector();
        if (world.rayTraceBlocks(hand, end, false, true, false) != null) {
            setDead();
            return;
        }
        if (isLatched()) {
            setPosition(end.x, end.y, end.z);
            motionX = motionY = motionZ = 0.0D;
            if (owner.getChainActionElapsed() >= EntityFireGiant.CHAIN_PULL_START_TICK) {
                if (++pullTicks > 20) { setDead(); return; }
                pullTarget(owner, target);
            }
        } else {
            fly(owner, target);
        }
    }

    private boolean isImmobilized(EntityPlayer target) {
        return PotionsItemRegistry.IMMOBILIZED != null
                && target.isPotionActive(PotionsItemRegistry.IMMOBILIZED);
    }

    private void fly(EntityFireGiant owner, EntityPlayer target) {
        Vec3d start = getPositionVector();
        Vec3d end = start.add(motionX, motionY, motionZ);
        if (!world.isAreaLoaded(new BlockPos(start).add(-1, -1, -1),
                new BlockPos(end).add(1, 1, 1))) { setDead(); return; }
        RayTraceResult blockHit = world.rayTraceBlocks(start, end, false, true, false);
        Vec3d collisionEnd = blockHit == null ? end : blockHit.hitVec;
        RayTraceResult hit = blockHit;
        double closest = start.squareDistanceTo(collisionEnd);
        for (Entity candidate : world.getEntitiesWithinAABBExcludingEntity(this,
                getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(0.3D))) {
            if (candidate == owner || candidate instanceof EntityFireGiant
                    || candidate instanceof IProjectile || candidate.isDead
                    || !candidate.canBeCollidedWith()) { continue; }
            AxisAlignedBB box = candidate.getEntityBoundingBox().grow(0.125D);
            RayTraceResult intercept = box.calculateIntercept(start, collisionEnd);
            Vec3d point = box.contains(start) ? start : intercept == null ? null : intercept.hitVec;
            if (point != null && start.squareDistanceTo(point) <= closest) {
                closest = start.squareDistanceTo(point);
                hit = new RayTraceResult(candidate, point);
            }
        }
        if (hit != null) {
            // A canceled impact consumes the hook too; it must never pass through a wall.
            if (ForgeEventFactory.onProjectileImpact(this, hit) || hit.entityHit != target
                    || world.rayTraceBlocks(owner.getCastingHandPosition(1.0F),
                    EntityFireGiant.getChainAimPosition(target), false, true, false) != null) {
                setDead();
            } else {
                dataManager.set(LATCHED, true);
                Vec3d attachment = EntityFireGiant.getChainAimPosition(target);
                setPosition(attachment.x, attachment.y, attachment.z);
                motionX = motionY = motionZ = 0.0D;
                velocityChanged = true;
            }
            return;
        }
        setPosition(end.x, end.y, end.z);
        if (owner.getDistanceSq(this) > 100.0D) { setDead(); }
    }

    private void pullTarget(EntityFireGiant owner, EntityPlayer target) {
        double dx = owner.posX - target.posX;
        double dz = owner.posZ - target.posZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= STOP_DISTANCE) { setDead(); return; }
        double desired = Math.min(0.55D, (distance - STOP_DISTANCE) * 0.35D);
        LivingKnockBackEvent event = ForgeHooks.onLivingKnockBack(target, owner,
                (float) desired, -dx, -dz);
        if (event.isCanceled()) { setDead(); return; }
        double resistance = MathHelper.clamp(target.getEntityAttribute(
                SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue(), 0.0D, 1.0D);
        double strength = Math.max(0.0D, event.getStrength()) * (1.0D - resistance);
        double ratioLength = Math.sqrt(event.getRatioX() * event.getRatioX()
                + event.getRatioZ() * event.getRatioZ());
        if (strength <= 0.0D || ratioLength < 1.0E-8D) { return; }
        double nextX = target.motionX * 0.3D - event.getRatioX() / ratioLength * strength;
        double nextZ = target.motionZ * 0.3D - event.getRatioZ() / ratioLength * strength;
        // Never force movement through terrain. Entity travel handles the actual movement.
        if (!world.getCollisionBoxes(target, target.getEntityBoundingBox()
                .expand(nextX, 0.0D, nextZ)).isEmpty()) { setDead(); return; }
        target.motionX = nextX;
        target.motionZ = nextZ;
        target.velocityChanged = true;
        target.isAirBorne = true;
        if (target instanceof EntityPlayerMP && ((EntityPlayerMP) target).connection != null) {
            ((EntityPlayerMP) target).connection.sendPacket(new SPacketEntityVelocity(target));
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) { }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        // A combat tether must not reconnect to a different entity after a chunk reload.
        age = MAX_LIFETIME;
    }
}
