package com.smd.gctcore.common.mixin.extrabotany;

import com.meteor.extrabotany.api.ExtraBotanyAPI;
import com.meteor.extrabotany.common.entity.EntitySubspace;
import com.meteor.extrabotany.common.entity.EntitySubspaceSpear;
import com.meteor.extrabotany.common.entity.EntityThrowableCopy;
import com.meteor.extrabotany.common.entity.gaia.EntityVoidHerrscher;
import com.meteor.extrabotany.common.item.ModItems;
import com.meteor.extrabotany.common.item.relic.ItemExcaliber;
import com.smd.gctcore.common.integration.extrabotany.IEntitySubspaceExt;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.botania.common.entity.EntityManaBurst;

/**
 * Ports Extra-Botany EntitySubspace damage/target extensions into Gct-Core.
 * Enables {@link com.smd.gctcore.common.integration.extrabotany.SubspaceHelper} custom aim & damage.
 */
@Mixin(value = EntitySubspace.class, remap = false)
public abstract class MixinEntitySubspace extends EntityThrowableCopy implements IEntitySubspaceExt {

    @Unique
    private static final String GCT$TAG_DAMAGE = "gct_damage";
    @Unique
    private static final String GCT$TAG_TARGET_X = "gct_targetX";
    @Unique
    private static final String GCT$TAG_TARGET_Y = "gct_targetY";
    @Unique
    private static final String GCT$TAG_TARGET_Z = "gct_targetZ";
    @Unique
    private static final String GCT$TAG_HAS_TARGET = "gct_hasTarget";

    @Unique
    private float gct$damage;
    @Unique
    private float gct$targetX;
    @Unique
    private float gct$targetY;
    @Unique
    private float gct$targetZ;
    @Unique
    private boolean gct$hasTarget;

    public MixinEntitySubspace(World worldIn) {
        super(worldIn);
    }

    @Shadow
    public abstract int getLiveTicks();

    @Shadow
    public abstract int getDelay();

    @Shadow
    public abstract int getType();

    @Shadow
    public abstract int getInterval();

    @Shadow
    public abstract int getCount();

    @Shadow
    public abstract void setCount(int count);

    /**
     * @author Gct-Core
     * @reason Support custom spear damage and aim target for type-1 subspace projectiles.
     * SRG: func_70071_h_ (MCP: onUpdate)
     */
    @Overwrite(aliases = "onUpdate")
    public void func_70071_h_() {
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;

        super.onUpdate();

        if (this.ticksExisted < getDelay()) {
            return;
        }

        if (this.ticksExisted > getLiveTicks() + getDelay()) {
            setDead();
        }

        EntityLivingBase thrower = getThrower();
        if (!this.world.isRemote && (thrower == null || thrower.isDead)) {
            setDead();
            return;
        }

        if (!this.world.isRemote) {
            if (getType() == 0) {
                if (this.ticksExisted % getInterval() == 0 && getCount() < 5
                        && this.ticksExisted > getDelay() + 5
                        && this.ticksExisted < getLiveTicks() - getDelay() - 10) {
                    if (!(thrower instanceof EntityPlayer)) {
                        setDead();
                        return;
                    }
                    EntityPlayer player = (EntityPlayer) getThrower();
                    if (ExtraBotanyAPI.cantAttack(player, player)) {
                        setDead();
                        return;
                    }
                    EntityManaBurst burst = ItemExcaliber.getBurst(player, new ItemStack(ModItems.excaliber));
                    burst.setPosition(this.posX, this.posY, this.posZ);
                    burst.setColor(0xFFAF00);
                    player.world.spawnEntity(burst);
                    setCount(getCount() + 1);
                }
            } else if (getType() == 1) {
                if (this.ticksExisted > getDelay() + 8 && getCount() < 1) {
                    EntitySubspaceSpear spear = new EntitySubspaceSpear(this.world, thrower);
                    float damage = gct$damage > 0 ? gct$damage : 12F;
                    if (thrower instanceof EntityVoidHerrscher) {
                        damage = 14F;
                        spear.setLiveTicks(1);
                    }
                    spear.setDamage(damage);
                    spear.setLife(100);

                    if (gct$hasTarget) {
                        double deltaX = gct$targetX - this.posX;
                        double deltaY = gct$targetY - (this.posY - 0.75F);
                        double deltaZ = gct$targetZ - this.posZ;
                        double length = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                        if (length > 0) {
                            double horizontalDistance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                            float pitch = (float) (MathHelper.atan2(deltaY, horizontalDistance) * (180D / Math.PI));
                            float yaw = (float) (MathHelper.atan2(deltaX, deltaZ) * (180D / Math.PI));

                            spear.rotationYaw = yaw;
                            spear.setPitch(-pitch);
                            spear.setRotation(MathHelper.wrapDegrees(-yaw + 180));
                            ((IProjectile) spear).shoot(deltaX / length, deltaY / length, deltaZ / length, 2.45F, 1.0F);
                        }
                    } else {
                        spear.rotationYaw = thrower.rotationYaw;
                        spear.setPitch(-thrower.rotationPitch);
                        spear.setRotation(MathHelper.wrapDegrees(-thrower.rotationYaw + 180));
                        spear.shoot(thrower, thrower.rotationPitch, thrower.rotationYaw, 0.0F, 2.45F, 1.0F);
                    }

                    spear.setPosition(this.posX, this.posY - 0.75F, this.posZ);
                    thrower.world.spawnEntity(spear);
                    setCount(getCount() + 1);
                }
            } else if (getType() == 2) {
                if (this.ticksExisted % getInterval() == 0 && getCount() < 6
                        && this.ticksExisted > getDelay() + 5
                        && this.ticksExisted < getLiveTicks() - getDelay() - 10) {
                    if (!(thrower instanceof EntityVoidHerrscher)) {
                        setDead();
                        return;
                    }
                    EntityVoidHerrscher herr = (EntityVoidHerrscher) getThrower();
                    if (herr.getPlayersAround().isEmpty()) {
                        setDead();
                        return;
                    }
                    if (ExtraBotanyAPI.cantAttack(thrower, herr.getPlayersAround().get(0))) {
                        setDead();
                        return;
                    }
                    EntityManaBurst burst = ItemExcaliber.getBurst(herr.getPlayersAround().get(0),
                            new ItemStack(ModItems.excaliber));
                    burst.setPosition(this.posX, this.posY, this.posZ);
                    burst.setColor(0xFFD700);
                    burst.shoot(thrower, thrower.rotationPitch + 15F, thrower.rotationYaw, 0F, 1F, 0F);
                    thrower.world.spawnEntity(burst);
                    setCount(getCount() + 1);
                }
            }
        }
    }

    /** SRG: func_70037_a (MCP: readEntityFromNBT) */
    @Inject(method = {"func_70037_a", "readEntityFromNBT"}, at = @At("RETURN"))
    private void gct$readExtra(NBTTagCompound cmp, CallbackInfo ci) {
        gct$damage = cmp.getFloat(GCT$TAG_DAMAGE);
        gct$targetX = cmp.getFloat(GCT$TAG_TARGET_X);
        gct$targetY = cmp.getFloat(GCT$TAG_TARGET_Y);
        gct$targetZ = cmp.getFloat(GCT$TAG_TARGET_Z);
        gct$hasTarget = cmp.getBoolean(GCT$TAG_HAS_TARGET);
    }

    /** SRG: func_70014_b (MCP: writeEntityToNBT) */
    @Inject(method = {"func_70014_b", "writeEntityToNBT"}, at = @At("RETURN"))
    private void gct$writeExtra(NBTTagCompound cmp, CallbackInfo ci) {
        cmp.setFloat(GCT$TAG_DAMAGE, gct$damage);
        cmp.setFloat(GCT$TAG_TARGET_X, gct$targetX);
        cmp.setFloat(GCT$TAG_TARGET_Y, gct$targetY);
        cmp.setFloat(GCT$TAG_TARGET_Z, gct$targetZ);
        cmp.setBoolean(GCT$TAG_HAS_TARGET, gct$hasTarget);
    }

    @Override
    public float getDamage() {
        return gct$damage;
    }

    @Override
    public void setDamage(float damage) {
        this.gct$damage = damage;
    }

    @Override
    public float getTargetX() {
        return gct$targetX;
    }

    @Override
    public void setTargetX(float x) {
        this.gct$targetX = x;
    }

    @Override
    public float getTargetY() {
        return gct$targetY;
    }

    @Override
    public void setTargetY(float y) {
        this.gct$targetY = y;
    }

    @Override
    public float getTargetZ() {
        return gct$targetZ;
    }

    @Override
    public void setTargetZ(float z) {
        this.gct$targetZ = z;
    }

    @Override
    public boolean getHasTarget() {
        return gct$hasTarget;
    }

    @Override
    public void setHasTarget(boolean hasTarget) {
        this.gct$hasTarget = hasTarget;
    }

    @Override
    public void setTarget(double x, double y, double z) {
        this.gct$targetX = (float) x;
        this.gct$targetY = (float) y;
        this.gct$targetZ = (float) z;
        this.gct$hasTarget = true;
    }
}
