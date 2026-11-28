package com.smd.gctcore.common.mixin.cyclops;

import com.smd.gctcore.common.integration.cyclops.CyclopsTruce;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The beam sets entities on fire even when their damage callback rejects the hit. */
@Pseudo
@Mixin(targets = "net.tangotek.cyclopstek.EntityAIEyeBeam", remap = false)
public abstract class MixinCyclopsTekEyeBeam {
    @Inject(method = "lambda$updateTask$5(Lnet/minecraft/entity/EntityLivingBase;)V", at = @At("HEAD"),
            cancellable = true, remap = false, require = 1)
    private void gctcore$skipAlliedBeamVictim(EntityLivingBase victim, CallbackInfo ci) {
        // This effect callback is always owned by Cyclops Tek.
        if (CyclopsTruce.isIceAndFireCyclops(victim)) ci.cancel();
    }
}
