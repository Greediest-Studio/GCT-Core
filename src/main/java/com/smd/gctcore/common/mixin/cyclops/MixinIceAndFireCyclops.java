package com.smd.gctcore.common.mixin.cyclops;

import com.smd.gctcore.common.integration.cyclops.CyclopsTruce;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Inherited target/damage guards and the species' own melee/eye entry points. */
@Pseudo
@Mixin(targets = "com.github.alexthe666.iceandfire.entity.EntityCyclops", remap = false)
public abstract class MixinIceAndFireCyclops extends EntityMob {
    protected MixinIceAndFireCyclops(World world) { super(world); }

    @Override
    public void setAttackTarget(EntityLivingBase target) {
        if (!CyclopsTruce.areAllies(this, target)) super.setAttackTarget(target);
    }

    @Override
    public void setRevengeTarget(EntityLivingBase target) {
        if (!CyclopsTruce.areAllies(this, target)) super.setRevengeTarget(target);
    }

    @Override
    public boolean isOnSameTeam(Entity other) {
        return CyclopsTruce.areAllies(this, other) || super.isOnSameTeam(other);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !CyclopsTruce.friendlyDamage(this, source) && super.attackEntityFrom(source, amount);
    }

    @Inject(method = {"attackEntityAsMob(Lnet/minecraft/entity/Entity;)Z",
            "func_70652_k(Lnet/minecraft/entity/Entity;)Z"}, at = @At("HEAD"),
            cancellable = true, remap = false, require = 1)
    private void gctcore$avoidAllyAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (CyclopsTruce.areAllies(this, target)) cir.setReturnValue(false);
    }

    @Inject(method = "onHitEye(Lnet/minecraft/util/DamageSource;F)V", at = @At("HEAD"),
            cancellable = true, remap = false, require = 1)
    private void gctcore$avoidFriendlyEyeHit(DamageSource source, float amount, CallbackInfo ci) {
        if (CyclopsTruce.friendlyDamage(this, source)) ci.cancel();
    }
}
