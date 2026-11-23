package com.smd.gctcore.common.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;

/** Non-homing spells and a short-lived gloom field; never changes terrain or starts fires. */
public class EntityGiantSpell extends Entity implements IProjectile {
    public static final byte FIREBALL = 0;
    public static final byte HEX = 1;
    public static final byte GLOOM = 2;
    private static final DataParameter<Byte> TYPE = EntityDataManager.createKey(
            EntityGiantSpell.class, DataSerializers.BYTE);
    private UUID ownerId;
    private EntityAnimatedGiant owner;
    private int age;
    private final Set<UUID> blinded = new HashSet<>();

    public EntityGiantSpell(World world) {
        super(world);
        setSize(0.4F, 0.4F);
        isImmuneToFire = true;
        setNoGravity(true);
    }

    @Override
    protected void entityInit() { dataManager.register(TYPE, FIREBALL); }
    public byte getSpellType() { return dataManager.get(TYPE); }

    public static void launch(EntityAnimatedGiant caster, byte type, EntityLivingBase target, float speed) {
        if (caster.world.isRemote || !caster.canHarm(target)) { return; }
        EntityGiantSpell spell = new EntityGiantSpell(caster.world);
        spell.owner = caster;
        spell.ownerId = caster.getUniqueID();
        spell.dataManager.set(TYPE, type);
        Vec3d direction = new Vec3d(target.posX - caster.posX, 0.0D,
                target.posZ - caster.posZ).normalize();
        // Spawn just beyond the caster's front, at a hand-height that can see ground targets.
        spell.setPosition(caster.posX + direction.x * (caster.width * 0.5D + 0.6D),
                caster.posY + caster.height * 0.6D,
                caster.posZ + direction.z * (caster.width * 0.5D + 0.6D));
        spell.shoot(target.posX - spell.posX,
                target.posY + target.height * 0.5D - spell.posY,
                target.posZ - spell.posZ, speed, 0.0F);
        caster.world.spawnEntity(spell);
    }

    public static void spawnGloom(EntityAnimatedGiant caster, Vec3d center) {
        if (caster.world.isRemote) { return; }
        EntityGiantSpell spell = new EntityGiantSpell(caster.world);
        spell.owner = caster;
        spell.ownerId = caster.getUniqueID();
        spell.dataManager.set(TYPE, GLOOM);
        spell.setPosition(center.x, center.y, center.z);
        caster.world.spawnEntity(spell);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3d direction = new Vec3d(x, y, z).normalize();
        motionX = (direction.x + rand.nextGaussian() * 0.0075D * inaccuracy) * velocity;
        motionY = (direction.y + rand.nextGaussian() * 0.0075D * inaccuracy) * velocity;
        motionZ = (direction.z + rand.nextGaussian() * 0.0075D * inaccuracy) * velocity;
        velocityChanged = true;
    }

    @Nullable
    private EntityAnimatedGiant getOwner() {
        if (owner == null && ownerId != null && world instanceof WorldServer) {
            Entity entity = ((WorldServer) world).getEntityFromUuid(ownerId);
            if (entity instanceof EntityAnimatedGiant) { owner = (EntityAnimatedGiant) entity; }
        }
        return owner;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        age++;
        if (world.isRemote) {
            if (getSpellType() == GLOOM) {
                for (int i = 0; i < 8; i++) {
                    double angle = rand.nextDouble() * Math.PI * 2.0D;
                    double radius = rand.nextDouble() * 4.0D;
                    world.spawnParticle(EnumParticleTypes.SPELL_WITCH, posX + Math.cos(angle) * radius,
                            posY + 0.15D, posZ + Math.sin(angle) * radius, 0.0D, 0.01D, 0.0D);
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    world.spawnParticle(getSpellType() == HEX ? EnumParticleTypes.SPELL_WITCH
                            : EnumParticleTypes.FLAME, posX - motionX * i * 0.2D,
                            posY - motionY * i * 0.2D, posZ - motionZ * i * 0.2D, 0.0D, 0.0D, 0.0D);
                }
                setPosition(posX + motionX, posY + motionY, posZ + motionZ);
            }
            return;
        }
        EntityAnimatedGiant caster = getOwner();
        if (caster == null || !caster.isEntityAlive() || age > (getSpellType() == GLOOM ? 100 : 60)) {
            setDead();
            return;
        }
        if (getSpellType() == GLOOM) {
            tickGloom(caster);
            return;
        }
        Vec3d start = getPositionVector();
        Vec3d end = start.add(motionX, motionY, motionZ);
        if (!world.isAreaLoaded(new net.minecraft.util.math.BlockPos(start).add(-1, -1, -1),
                new net.minecraft.util.math.BlockPos(end).add(1, 1, 1))) {
            setDead();
            return;
        }
        RayTraceResult blockHit = world.rayTraceBlocks(start, end, false, true, false);
        Vec3d collisionEnd = blockHit == null ? end : blockHit.hitVec;
        double closest = start.squareDistanceTo(collisionEnd);
        RayTraceResult hit = blockHit;
        for (Entity candidate : world.getEntitiesWithinAABBExcludingEntity(this,
                getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(0.5D))) {
            if (!(candidate instanceof EntityLivingBase) || !caster.canHarm((EntityLivingBase) candidate)
                    || !candidate.canBeCollidedWith()) { continue; }
            AxisAlignedBB box = candidate.getEntityBoundingBox().grow(0.2D);
            RayTraceResult intercept = box.calculateIntercept(start, collisionEnd);
            Vec3d point = box.contains(start) ? start : intercept == null ? null : intercept.hitVec;
            if (point != null && start.squareDistanceTo(point) <= closest) {
                closest = start.squareDistanceTo(point);
                hit = new RayTraceResult(candidate, point);
            }
        }
        if (hit != null && !ForgeEventFactory.onProjectileImpact(this, hit)) {
            impact(caster, hit);
            setDead();
            return;
        }
        setPosition(end.x, end.y, end.z);
    }

    private boolean damage(EntityAnimatedGiant caster, EntityLivingBase target, float amount) {
        if (!caster.canHarm(target)) { return false; }
        DamageSource source = new EntityDamageSourceIndirect("indirectMagic", this, caster) {
            @Override
            public boolean isDifficultyScaled() { return false; }
        }.setMagicDamage();
        if (getSpellType() != GLOOM) { source.setProjectile(); }
        return target.attackEntityFrom(source, amount);
    }

    private void impact(EntityAnimatedGiant caster, RayTraceResult hit) {
        Vec3d center = hit.hitVec;
        if (getSpellType() == HEX) {
            if (hit.entityHit instanceof EntityLivingBase) {
                EntityLivingBase target = (EntityLivingBase) hit.entityHit;
                if (damage(caster, target, 6.0F)) {
                    target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 80, 1));
                    target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 80, 0));
                }
            }
            caster.particles(EnumParticleTypes.SPELL_WITCH, center, 18);
            playSound(SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 0.8F, 0.7F);
        } else {
            // Exactly one damage call per victim: a direct hit never also takes splash damage.
            if (hit.entityHit instanceof EntityLivingBase) {
                damage(caster, (EntityLivingBase) hit.entityHit, 16.0F);
            }
            for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class,
                    new AxisAlignedBB(center, center).grow(2.5D))) {
                if (target == hit.entityHit || !caster.canHarm(target)
                        || target.getDistanceSq(center.x, center.y, center.z) > 6.25D
                        || !hasLineOfSight(center, target)) { continue; }
                damage(caster, target, 8.0F);
            }
            caster.particles(EnumParticleTypes.EXPLOSION_LARGE, center, 3);
            caster.particles(EnumParticleTypes.FLAME, center, 24);
            playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.4F, 1.2F);
        }
    }

    private void tickGloom(EntityAnimatedGiant caster) {
        Vec3d center = getPositionVector();
        if (age % 5 == 0) { caster.telegraph(center, 4.0D, EnumParticleTypes.SPELL_WITCH); }
        for (EntityLivingBase target : world.getEntitiesWithinAABB(EntityLivingBase.class,
                new AxisAlignedBB(posX - 4.0D, posY - 1.0D, posZ - 4.0D,
                        posX + 4.0D, posY + 3.0D, posZ + 4.0D))) {
            double dx = target.posX - posX;
            double dz = target.posZ - posZ;
            if (!caster.canHarm(target) || dx * dx + dz * dz > 16.0D
                    || !hasLineOfSight(center.add(0.0D, 0.5D, 0.0D), target)) { continue; }
            if (blinded.add(target.getUniqueID())) {
                target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 20, 0));
            }
            if (age % 20 == 0) { damage(caster, target, 2.0F); }
        }
    }

    private boolean hasLineOfSight(Vec3d from, EntityLivingBase target) {
        return world.rayTraceBlocks(from, target.getPositionVector().add(0.0D,
                target.height * 0.5D, 0.0D), false, true, false) == null;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        if (ownerId != null) { tag.setUniqueId("Owner", ownerId); }
        tag.setByte("SpellType", getSpellType());
        tag.setInteger("Age", age);
        net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
        for (UUID id : blinded) { list.appendTag(new net.minecraft.nbt.NBTTagString(id.toString())); }
        tag.setTag("Blinded", list);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        ownerId = tag.hasUniqueId("Owner") ? tag.getUniqueId("Owner") : null;
        owner = null;
        dataManager.set(TYPE, (byte) Math.max(0, Math.min(2, tag.getByte("SpellType"))));
        age = Math.max(0, tag.getInteger("Age"));
        blinded.clear();
        net.minecraft.nbt.NBTTagList list = tag.getTagList("Blinded", 8);
        for (int i = 0; i < list.tagCount(); i++) {
            try { blinded.add(UUID.fromString(list.getStringTagAt(i))); }
            catch (IllegalArgumentException ignored) { }
        }
    }
}
