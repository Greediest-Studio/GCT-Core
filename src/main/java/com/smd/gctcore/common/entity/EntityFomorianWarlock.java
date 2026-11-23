package com.smd.gctcore.common.entity;

import java.util.ArrayList;
import java.util.List;

import com.smd.gctcore.misc.SoundRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** A ranged pact caster whose blood rune can be broken by repeated melee hits. */
public class EntityFomorianWarlock extends EntityAnimatedGiant {

    public static final byte MELEE = 1;
    public static final byte HEX = 2;
    public static final byte BLAST = 3;
    public static final byte GLOOM = 4;
    private static final int MELEE_COOLDOWN = 60;
    private static final int SPELL_INTERVAL = 80;
    private static final int HEX_COOLDOWN = 240;
    private static final int BLAST_COOLDOWN = 160;
    private static final int GLOOM_COOLDOWN = 320;
    private static final int BLOOD_REFLECT_COOLDOWN = 160;
    private static final int BLOOD_RESTORE_TICKS = 400;
    private static final int POINT_LOCK_TICK = 10;
    private static final double SPELL_RANGE_SQ = 24.0D * 24.0D;
    private static final DataParameter<Boolean> BLOOD_RUNE_BROKEN = EntityDataManager.createKey(
            EntityFomorianWarlock.class, DataSerializers.BOOLEAN);

    private int meleeReady;
    private int spellReady;
    private int hexReady;
    private int blastReady;
    private int gloomReady;
    private int bloodReflectReady;
    private int bloodRuneMeleeHits;
    private int bloodRestoreTicks;

    public EntityFomorianWarlock(World world) {
        super(world);
        setSize(2.0F, 5.0F);
        stepHeight = 1.0F;
        experienceValue = 20;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(BLOOD_RUNE_BROKEN, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(180.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(5.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.24D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
    }

    @Override
    protected String getAnimationPrefix() {
        return "animation.fomorian_warlock.";
    }

    @Override
    protected String getGiantFaction() {
        return "fomorian";
    }

    @Override
    protected double getAcquireRange() {
        return 32.0D;
    }

    @Override
    protected double getMeleeReach() {
        return 5.0D;
    }

    @Override
    protected String getActionAnimation(byte action) {
        return action == MELEE ? "melee_attack" : "pact_cast";
    }

    @Override
    protected int getActionLength(byte action) {
        return action == MELEE ? 45 : 52;
    }

    @Override
    protected int getActionImpactTick(byte action) {
        return action == MELEE ? 25 : 26;
    }

    public boolean isBloodRuneBroken() {
        return dataManager.get(BLOOD_RUNE_BROKEN);
    }

    @Override
    protected byte chooseAction(EntityLivingBase target) {
        if (!isValidCombatTarget(target) || !getEntitySenses().canSee(target)) {
            return 0;
        }
        double distanceSq = getDistanceSq(target);
        if (distanceSq <= 25.0D && ticksExisted >= meleeReady) {
            return MELEE;
        }
        if (ticksExisted < spellReady) {
            return 0;
        }
        List<Byte> available = new ArrayList<>();
        if (!isBloodRuneBroken() && ticksExisted >= hexReady && distanceSq <= SPELL_RANGE_SQ) {
            available.add(HEX);
        }
        if (distanceSq <= SPELL_RANGE_SQ) {
            if (ticksExisted >= blastReady) {
                available.add(BLAST);
            }
            if (ticksExisted >= gloomReady) {
                available.add(GLOOM);
            }
        }
        return available.isEmpty() ? 0 : available.get(rand.nextInt(available.size()));
    }

    @Override
    protected void onActionStarted(byte action, EntityLivingBase target) {
        if (action == MELEE) {
            meleeReady = ticksExisted + MELEE_COOLDOWN;
            playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.2F, 0.55F);
            return;
        }
        spellReady = ticksExisted + SPELL_INTERVAL;
        if (action == HEX) {
            hexReady = ticksExisted + HEX_COOLDOWN;
        } else if (action == BLAST) {
            blastReady = ticksExisted + BLAST_COOLDOWN;
        } else if (action == GLOOM) {
            gloomReady = ticksExisted + GLOOM_COOLDOWN;
        }
        playSound(SoundEvents.EVOCATION_ILLAGER_PREPARE_ATTACK, 1.2F, 0.65F);
    }

    @Override
    protected void onActionTick(byte action, int elapsed, EntityLivingBase target) {
        if (action == MELEE) {
            return;
        }
        // Follow the target for the first half second, then leave a fixed marker
        // which players can escape before the effect arrives at tick 26.
        if (elapsed <= POINT_LOCK_TICK && isValidCombatTarget(target)) {
            actionPoint = target.getPositionVector();
        }
        if (actionPoint != null && elapsed < getActionImpactTick(action) && elapsed % 2 == 0) {
            telegraph(actionPoint, action == BLAST ? 3.0D : action == GLOOM ? 4.0D : 0.8D,
                    action == GLOOM ? EnumParticleTypes.SMOKE_NORMAL : EnumParticleTypes.SPELL_WITCH);
        }
        if (elapsed % 4 == 0) {
            particles(EnumParticleTypes.SPELL_WITCH,
                    new Vec3d(posX, posY + height * 0.74D, posZ), 5);
        }
    }

    @Override
    protected boolean shouldTrackTargetDuringAction(byte action, int elapsed) {
        return action == MELEE || action == HEX || elapsed <= POINT_LOCK_TICK;
    }

    @Override
    protected void resolveAction(byte action, EntityLivingBase target) {
        if (action == MELEE) {
            if (canMeleeHit(target, 5.0D, 90.0F) && hurtTarget(target, 12.0F)) {
                knockAway(target, 0.65F);
            }
            return;
        }
        playSound(SoundEvents.EVOCATION_ILLAGER_CAST_SPELL, 1.3F, 0.6F);
        if (action == HEX) {
            // Breaking the rune interrupts even a curse already being prepared.
            if (!isBloodRuneBroken() && isValidCombatTarget(target)
                    && getDistanceSq(target) <= SPELL_RANGE_SQ
                    && getEntitySenses().canSee(target)) {
                EntityGiantSpell.launch(this, EntityGiantSpell.HEX, target, 1.1F);
            }
        } else if (action == BLAST && actionPoint != null) {
            releaseBlast();
        } else if (action == GLOOM && actionPoint != null) {
            EntityGiantSpell.spawnGloom(this, actionPoint);
        }
    }

    private void releaseBlast() {
        particles(EnumParticleTypes.EXPLOSION_LARGE, actionPoint.add(0.0D, 0.5D, 0.0D), 4);
        particles(EnumParticleTypes.SPELL_WITCH, actionPoint.add(0.0D, 0.7D, 0.0D), 32);
        playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.1F, 0.7F);
        AxisAlignedBB area = new AxisAlignedBB(actionPoint.x - 3.0D, actionPoint.y - 3.0D,
                actionPoint.z - 3.0D, actionPoint.x + 3.0D, actionPoint.y + 3.0D,
                actionPoint.z + 3.0D);
        for (EntityLivingBase victim : world.getEntitiesWithinAABB(EntityLivingBase.class, area)) {
            if (!canHarm(victim)
                    || victim.getPositionVector().squareDistanceTo(actionPoint) > 9.0D) {
                continue;
            }
            Vec3d victimCenter = victim.getPositionVector().add(0.0D, victim.height * 0.5D, 0.0D);
            if (world.rayTraceBlocks(actionPoint.add(0.0D, 0.3D, 0.0D),
                    victimCenter, false, true, false) != null) {
                continue;
            }
            if (hurtTarget(victim, 14.0F)) {
                applyGiantKnockback(this, victim,
                        victim.posX - actionPoint.x, victim.posZ - actionPoint.z, 1.1F);
            }
        }
    }

    @Override
    protected void updateCombatMovement(EntityLivingBase target) {
        keepDistance(target, 8.0D, 16.0D);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        float before = getHealth();
        boolean accepted = super.attackEntityFrom(source, amount);
        if (!accepted || world.isRemote || !isEntityAlive() || getHealth() >= before
                || !(source.getTrueSource() instanceof EntityPlayer) || isBloodRuneBroken()) {
            return accepted;
        }
        EntityPlayer player = (EntityPlayer) source.getTrueSource();
        if (!canHarm(player)) {
            return accepted;
        }
        if (ticksExisted >= bloodReflectReady) {
            player.addPotionEffect(new PotionEffect(MobEffects.POISON, 60, 0));
            bloodReflectReady = ticksExisted + BLOOD_REFLECT_COOLDOWN;
            particles(EnumParticleTypes.SPELL_WITCH, player.getPositionVector().add(0, 1, 0), 14);
        }
        boolean melee = source instanceof EntityDamageSource && source.getImmediateSource() == player
                && !source.isProjectile() && !source.isExplosion() && !source.isMagicDamage();
        if (melee && ++bloodRuneMeleeHits >= 4) {
            bloodRuneMeleeHits = 0;
            bloodRestoreTicks = BLOOD_RESTORE_TICKS;
            dataManager.set(BLOOD_RUNE_BROKEN, true);
            particles(EnumParticleTypes.CRIT, getPositionVector().add(0, height * 0.57D, 0), 30);
            particles(EnumParticleTypes.REDSTONE, getPositionVector().add(0, height * 0.57D, 0), 18);
            playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.2F, 0.65F);
        }
        return accepted;
    }

    @Override
    public void onLivingUpdate() {
        if (!world.isRemote && isEntityAlive() && bloodRestoreTicks > 0 && --bloodRestoreTicks == 0) {
            dataManager.set(BLOOD_RUNE_BROKEN, false);
            bloodRuneMeleeHits = 0;
            particles(EnumParticleTypes.SPELL_WITCH, getPositionVector().add(0, height * 0.57D, 0), 30);
            playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.1F, 0.6F);
        }
        super.onLivingUpdate();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("BloodRuneMeleeHits", bloodRuneMeleeHits);
        tag.setInteger("BloodRuneRestore", bloodRestoreTicks);
        tag.setInteger("BloodRuneReflectCooldown", Math.max(0, bloodReflectReady - ticksExisted));
        tag.setInteger("MeleeCooldown", Math.max(0, meleeReady - ticksExisted));
        tag.setInteger("SpellCooldown", Math.max(0, spellReady - ticksExisted));
        tag.setInteger("HexCooldown", Math.max(0, hexReady - ticksExisted));
        tag.setInteger("BlastCooldown", Math.max(0, blastReady - ticksExisted));
        tag.setInteger("GloomCooldown", Math.max(0, gloomReady - ticksExisted));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        bloodRuneMeleeHits = Math.max(0, Math.min(3, tag.getInteger("BloodRuneMeleeHits")));
        bloodRestoreTicks = Math.max(0, Math.min(BLOOD_RESTORE_TICKS, tag.getInteger("BloodRuneRestore")));
        dataManager.set(BLOOD_RUNE_BROKEN, bloodRestoreTicks > 0);
        bloodReflectReady = ticksExisted + Math.max(0, Math.min(BLOOD_REFLECT_COOLDOWN,
                tag.getInteger("BloodRuneReflectCooldown")));
        meleeReady = ticksExisted + Math.max(0, Math.min(MELEE_COOLDOWN, tag.getInteger("MeleeCooldown")));
        spellReady = ticksExisted + Math.max(0, Math.min(SPELL_INTERVAL, tag.getInteger("SpellCooldown")));
        hexReady = ticksExisted + Math.max(0, Math.min(HEX_COOLDOWN, tag.getInteger("HexCooldown")));
        blastReady = ticksExisted + Math.max(0, Math.min(BLAST_COOLDOWN, tag.getInteger("BlastCooldown")));
        gloomReady = ticksExisted + Math.max(0, Math.min(GLOOM_COOLDOWN, tag.getInteger("GloomCooldown")));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.FOMORIAN_WARLOCK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.FOMORIAN_WARLOCK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.FOMORIAN_WARLOCK_DEATH;
    }
}
