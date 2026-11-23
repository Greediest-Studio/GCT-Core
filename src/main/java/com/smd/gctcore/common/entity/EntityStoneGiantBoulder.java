package com.smd.gctcore.common.entity;

import com.smd.gctcore.common.util.CloudGiantBallistics;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.UUID;

/** A heavy physical rock: its launch is aimed once, then only gravity changes its course. */
public class EntityStoneGiantBoulder extends Entity implements IProjectile {

    public static final float SHOT_SPEED = 1.4F;
    public static final float DAMAGE = 14.0F;
    private static final double GRAVITY = 0.04D;
    private static final int MAX_FLIGHT_TICKS = 160;
    private static final float IMPACT_KNOCKBACK = 0.9F;
    private EntityLivingBase shooter;
    private UUID shooterId;
    private int flightTicks;

    public EntityStoneGiantBoulder(World world) {
        super(world);
        setSize(0.9F, 0.9F);
        isImmuneToFire = true;
    }

    public EntityStoneGiantBoulder(World world, EntityLivingBase shooter) {
        this(world);
        this.shooter = shooter;
        shooterId = shooter.getUniqueID();
    }

    @Override
    protected void entityInit() {
    }

    @Nullable
    public static Vec3d getLaunchVelocity(Vec3d displacement) {
        double[] velocity = CloudGiantBallistics.getLaunchVelocity(displacement.x, displacement.y,
                displacement.z, SHOT_SPEED, GRAVITY, MAX_FLIGHT_TICKS);
        return velocity == null ? null : new Vec3d(velocity[0], velocity[1], velocity[2]);
    }

    @Override
    public void shoot(double x, double y, double z, float speed, float inaccuracy) {
        Vec3d direction = new Vec3d(x, y, z).normalize();
        motionX = (direction.x + rand.nextGaussian() * 0.0075D * inaccuracy) * speed;
        motionY = (direction.y + rand.nextGaussian() * 0.0075D * inaccuracy) * speed;
        motionZ = (direction.z + rand.nextGaussian() * 0.0075D * inaccuracy) * speed;
        flightTicks = 0;
        updateRotation();
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
        velocityChanged = true;
    }

    private void updateRotation() {
        rotationYaw = (float) Math.toDegrees(Math.atan2(motionX, motionZ));
        rotationPitch = (float) Math.toDegrees(Math.atan2(motionY,
                Math.sqrt(motionX * motionX + motionZ * motionZ)));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (isDead) {
            return;
        }
        if (!world.isRemote && ++flightTicks > MAX_FLIGHT_TICKS) {
            shatter();
            return;
        }
        Vec3d start = getPositionVector().add(0.0D, height * 0.5D, 0.0D);
        Vec3d end = start.add(motionX, motionY, motionZ);
        if (!world.isRemote) {
            if (!world.isAreaLoaded(new net.minecraft.util.math.BlockPos(start).add(-1, -1, -1),
                    new net.minecraft.util.math.BlockPos(end).add(1, 1, 1))) {
                setDead();
                return;
            }
            RayTraceResult blockHit = findBlockHit(start, end);
            RayTraceResult hit = findEntityHit(start, blockHit == null ? end : blockHit.hitVec);
            if (hit == null) {
                hit = blockHit;
            }
            if (hit != null && !ForgeEventFactory.onProjectileImpact(this, hit)) {
                setPosition(hit.hitVec.x, hit.hitVec.y - height * 0.5D, hit.hitVec.z);
                if (hit.entityHit != null) {
                    hitEntity(hit.entityHit);
                }
                shatter();
                return;
            }
        }
        setPosition(end.x, end.y - height * 0.5D, end.z);
        updateRotation();
        if (!hasNoGravity()) {
            motionY -= GRAVITY;
        }
    }

    @Nullable
    private RayTraceResult findBlockHit(Vec3d start, Vec3d end) {
        // Trace the broad rock's center and six extrema rather than a single narrow ray.
        double radius = width * 0.5D;
        Vec3d[] offsets = {Vec3d.ZERO, new Vec3d(radius, 0, 0), new Vec3d(-radius, 0, 0),
                new Vec3d(0, radius, 0), new Vec3d(0, -radius, 0),
                new Vec3d(0, 0, radius), new Vec3d(0, 0, -radius)};
        RayTraceResult nearest = null;
        double distance = start.squareDistanceTo(end);
        for (Vec3d offset : offsets) {
            RayTraceResult result = world.rayTraceBlocks(start.add(offset), end.add(offset), false, true, false);
            if (result != null) {
                Vec3d centerAtImpact = result.hitVec.subtract(offset);
                double candidateDistance = start.squareDistanceTo(centerAtImpact);
                if (candidateDistance <= distance) {
                    distance = candidateDistance;
                    nearest = new RayTraceResult(centerAtImpact, result.sideHit, result.getBlockPos());
                }
            }
        }
        return nearest;
    }

    @Nullable
    private RayTraceResult findEntityHit(Vec3d start, Vec3d end) {
        AxisAlignedBB swept = getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(0.5D);
        RayTraceResult nearest = null;
        double distance = start.squareDistanceTo(end);
        for (Entity candidate : world.getEntitiesWithinAABBExcludingEntity(this, swept)) {
            if (candidate instanceof EntityStoneGiant || candidate instanceof IProjectile
                    || candidate == shooter || candidate.getUniqueID().equals(shooterId)
                    || candidate.isDead || !candidate.canBeCollidedWith()
                    || candidate instanceof EntityPlayer && ((EntityPlayer) candidate).isSpectator()) {
                continue;
            }
            AxisAlignedBB box = candidate.getEntityBoundingBox().grow(width * 0.5D);
            RayTraceResult intercept = box.calculateIntercept(start, end);
            Vec3d location = box.contains(start) ? start : intercept == null ? null : intercept.hitVec;
            if (location != null && start.squareDistanceTo(location) <= distance) {
                distance = start.squareDistanceTo(location);
                nearest = new RayTraceResult(candidate, location);
            }
        }
        return nearest;
    }

    @Nullable
    private EntityLivingBase getShooter() {
        if (shooter == null && shooterId != null && world instanceof WorldServer) {
            Entity found = ((WorldServer) world).getEntityFromUuid(shooterId);
            if (found instanceof EntityLivingBase) {
                shooter = (EntityLivingBase) found;
            }
        }
        return shooter;
    }

    private void hitEntity(Entity target) {
        if (target instanceof EntityStoneGiant || target instanceof EntityPlayer
                && (((EntityPlayer) target).capabilities.disableDamage || ((EntityPlayer) target).isSpectator())) {
            return;
        }
        DamageSource source = new EntityDamageSourceIndirect("arrow", this, getShooter()) {
            @Override
            public boolean isDifficultyScaled() {
                return false;
            }
        }.setProjectile();
        double previousX = target.motionX;
        double previousY = target.motionY;
        double previousZ = target.motionZ;
        if (target.attackEntityFrom(source, DAMAGE) && target instanceof EntityLivingBase) {
            target.motionX = previousX;
            target.motionY = previousY;
            target.motionZ = previousZ;
            EntityAnimatedGiant.applyGiantKnockback(getShooter() == null ? this : getShooter(),
                    (EntityLivingBase) target, motionX, motionZ, IMPACT_KNOCKBACK);
        }
    }

    private void shatter() {
        playSound(SoundEvents.BLOCK_STONE_BREAK, 1.1F, 0.65F);
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(EnumParticleTypes.BLOCK_CRACK,
                    posX, posY + height * 0.5D, posZ, 18, 0.3D, 0.3D, 0.3D, 0.08D,
                    Block.getStateId(Blocks.STONE.getDefaultState()));
        }
        setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (shooterId != null) {
            compound.setUniqueId("Shooter", shooterId);
        }
        compound.setInteger("FlightTicks", flightTicks);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        shooterId = compound.hasUniqueId("Shooter") ? compound.getUniqueId("Shooter") : null;
        shooter = null;
        flightTicks = Math.max(0, compound.getInteger("FlightTicks"));
    }
}
