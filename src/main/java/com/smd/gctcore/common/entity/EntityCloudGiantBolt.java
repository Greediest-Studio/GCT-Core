package com.smd.gctcore.common.entity;

import com.smd.gctcore.common.util.CloudGiantBallistics;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

/** A physical, non-homing crossbow bolt. Cloud giant allies are excluded before ray tracing. */
public class EntityCloudGiantBolt extends Entity implements IProjectile {

    public static final float SHOT_SPEED = 2.4F;
    public static final float DAMAGE = 8.0F;
    private static final double GRAVITY = 0.03D;
    private static final int MAX_FLIGHT_TICKS = 160;
    private static final int GROUND_LIFETIME = 6;
    private static final float KNOCKBACK = 1.25F;
    private static final DataParameter<Boolean> IN_GROUND = EntityDataManager.createKey(
            EntityCloudGiantBolt.class, DataSerializers.BOOLEAN);

    private EntityLivingBase shooter;
    private UUID shooterId;
    private int flightTicks;
    private int groundTicks;

    public EntityCloudGiantBolt(World world) {
        super(world);
        setSize(0.25F, 0.25F);
        isImmuneToFire = true;
    }

    public EntityCloudGiantBolt(World world, EntityLivingBase shooter) {
        this(world);
        this.shooter = shooter;
        this.shooterId = shooter.getUniqueID();
    }

    @Override
    protected void entityInit() {
        dataManager.register(IN_GROUND, false);
    }

    @Nullable
    public static Vec3d getLaunchVelocity(Vec3d displacement) {
        double[] velocity = CloudGiantBallistics.getLaunchVelocity(
                displacement.x, displacement.y, displacement.z,
                SHOT_SPEED, GRAVITY, MAX_FLIGHT_TICKS);
        return velocity == null ? null : new Vec3d(velocity[0], velocity[1], velocity[2]);
    }

    @Override
    public void shoot(double x, double y, double z, float speed, float inaccuracy) {
        Vec3d direction = new Vec3d(x, y, z).normalize();
        motionX = (direction.x + rand.nextGaussian() * 0.0075D * inaccuracy) * speed;
        motionY = (direction.y + rand.nextGaussian() * 0.0075D * inaccuracy) * speed;
        motionZ = (direction.z + rand.nextGaussian() * 0.0075D * inaccuracy) * speed;
        dataManager.set(IN_GROUND, false);
        flightTicks = groundTicks = 0;
        updateRotation();
        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z) {
        super.setVelocity(x, y, z);
        if (x * x + y * y + z * z > 1.0E-8D) {
            updateRotation();
        }
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
        if (dataManager.get(IN_GROUND)) {
            motionX = motionY = motionZ = 0.0D;
            if (!world.isRemote && ++groundTicks >= GROUND_LIFETIME) {
                setDead();
            }
            return;
        }
        if (!world.isRemote && ++flightTicks > MAX_FLIGHT_TICKS) {
            setDead();
            return;
        }

        Vec3d start = getPositionVector();
        Vec3d end = start.add(motionX, motionY, motionZ);
        if (!world.isRemote) {
            RayTraceResult blockHit = world.rayTraceBlocks(start, end, false, true, false);
            RayTraceResult hit = findEntityHit(start, blockHit == null ? end : blockHit.hitVec);
            if (hit == null) {
                hit = blockHit;
            }
            if (hit != null) {
                boolean canceled = ForgeEventFactory.onProjectileImpact(this, hit);
                if (hit.entityHit != null) {
                    if (!canceled) {
                        hitEntity(hit.entityHit);
                    }
                    // Even invulnerable entities or a blocked shot consume the bolt.
                    setDead();
                    return;
                } else if (!canceled) {
                    Vec3d position = hit.hitVec.subtract(new Vec3d(motionX, motionY, motionZ)
                            .normalize().scale(0.04D));
                    setPosition(position.x, position.y, position.z);
                    motionX = motionY = motionZ = 0.0D;
                    dataManager.set(IN_GROUND, true);
                    velocityChanged = true;
                    playSound(SoundEvents.ENTITY_ARROW_HIT, 0.8F, 0.8F);
                    return;
                }
            }
        }

        setPosition(end.x, end.y, end.z);
        updateRotation();
        if (!hasNoGravity()) {
            motionY -= GRAVITY;
        }
    }

    @Nullable
    private RayTraceResult findEntityHit(Vec3d start, Vec3d end) {
        AxisAlignedBB swept = getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(1.0D);
        RayTraceResult nearest = null;
        double nearestDistance = start.squareDistanceTo(end);
        for (Entity candidate : world.getEntitiesWithinAABBExcludingEntity(this, swept)) {
            // Filtering here allows a bolt to hit a player behind any number of allies.
            if (candidate instanceof ICloudGiantAlly || candidate == shooter
                    || candidate.getUniqueID().equals(shooterId) || candidate instanceof IProjectile
                    || candidate.isDead || !candidate.canBeCollidedWith()
                    || candidate instanceof EntityPlayer && ((EntityPlayer) candidate).isSpectator()) {
                continue;
            }
            AxisAlignedBB box = candidate.getEntityBoundingBox().grow(0.15D);
            RayTraceResult intercept = box.calculateIntercept(start, end);
            Vec3d hitPosition = box.contains(start) ? start : intercept == null ? null : intercept.hitVec;
            if (hitPosition != null) {
                double distance = start.squareDistanceTo(hitPosition);
                if (distance <= nearestDistance) {
                    nearestDistance = distance;
                    nearest = new RayTraceResult(candidate, hitPosition);
                }
            }
        }
        return nearest;
    }

    @Nullable
    private EntityLivingBase getShooter() {
        if (shooter == null && shooterId != null && world instanceof WorldServer) {
            Entity entity = ((WorldServer) world).getEntityFromUuid(shooterId);
            if (entity instanceof EntityLivingBase) {
                shooter = (EntityLivingBase) entity;
            }
        }
        return shooter;
    }

    private void hitEntity(Entity target) {
        if (target instanceof ICloudGiantAlly
                || target instanceof EntityPlayer && ((EntityPlayer) target).capabilities.disableDamage) {
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
            // Replace the vanilla random-resistance impulse with one proportional impulse.
            target.motionX = previousX;
            target.motionY = previousY;
            target.motionZ = previousZ;
            applyKnockback((EntityLivingBase) target);
            target.velocityChanged = true;
            if (target instanceof EntityPlayerMP) {
                ((EntityPlayerMP) target).connection.sendPacket(new SPacketEntityVelocity(target));
            }
        }
    }

    private void applyKnockback(EntityLivingBase target) {
        double dx = motionX;
        double dz = motionZ;
        if (dx * dx + dz * dz < 1.0E-8D) {
            dx = target.posX - posX;
            dz = target.posZ - posZ;
        }
        if (dx * dx + dz * dz < 1.0E-8D) {
            dx = 1.0D;
        }
        LivingKnockBackEvent event = ForgeHooks.onLivingKnockBack(target,
                getShooter() == null ? this : getShooter(), KNOCKBACK, -dx, -dz);
        if (event.isCanceled()) {
            return;
        }
        double resistance = MathHelper.clamp(target.getEntityAttribute(
                SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue(), 0.0D, 1.0D);
        double strength = Math.max(0.0D, event.getStrength()) * (1.0D - resistance);
        double length = Math.sqrt(event.getRatioX() * event.getRatioX()
                + event.getRatioZ() * event.getRatioZ());
        if (strength <= 0.0D || length < 1.0E-8D) {
            return;
        }
        target.motionX = target.motionX * 0.5D - event.getRatioX() / length * strength;
        target.motionZ = target.motionZ * 0.5D - event.getRatioZ() / length * strength;
        target.motionY += MathHelper.clamp(event.getStrength() * 0.28D, 0.0D, 0.35D)
                * (1.0D - resistance);
        target.isAirBorne = true;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        if (shooterId != null) {
            compound.setUniqueId("Shooter", shooterId);
        }
        compound.setBoolean("InGround", dataManager.get(IN_GROUND));
        compound.setInteger("FlightTicks", flightTicks);
        compound.setInteger("GroundTicks", groundTicks);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        shooterId = compound.hasUniqueId("Shooter") ? compound.getUniqueId("Shooter") : null;
        shooter = null;
        dataManager.set(IN_GROUND, compound.getBoolean("InGround"));
        flightTicks = Math.max(0, compound.getInteger("FlightTicks"));
        groundTicks = Math.max(0, compound.getInteger("GroundTicks"));
    }
}
