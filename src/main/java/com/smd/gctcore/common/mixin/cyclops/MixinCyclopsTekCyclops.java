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

@Pseudo
@Mixin(targets = "net.tangotek.cyclopstek.EntityCyclops", remap = false)
public abstract class MixinCyclopsTekCyclops extends EntityMob {
    protected MixinCyclopsTekCyclops(World world) { super(world); }

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

    @Inject(method = {"attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z",
            "func_70097_a(Lnet/minecraft/util/DamageSource;F)Z"}, at = @At("HEAD"),
            cancellable = true, remap = false, require = 1)
    private void gctcore$avoidFriendlyDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CyclopsTruce.friendlyDamage(this, source)) cir.setReturnValue(false);
    }

    @Inject(method = {"attackEntityAsMob(Lnet/minecraft/entity/Entity;)Z",
            "func_70652_k(Lnet/minecraft/entity/Entity;)Z"}, at = @At("HEAD"),
            cancellable = true, remap = false, require = 1)
    private void gctcore$avoidAllyAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (CyclopsTruce.areAllies(this, target)) cir.setReturnValue(false);
    }

    /** Damage and block-placement reactions share this private threat accumulator. */
    @Inject(method = "addAggro(Lnet/minecraft/entity/EntityLivingBase;F)V", at = @At("HEAD"),
            cancellable = true, remap = false, require = 1)
    private void gctcore$ignoreAlliedThreat(EntityLivingBase source, float amount, CallbackInfo ci) {
        if (CyclopsTruce.areAllies(this, source)) ci.cancel();
    }
}
