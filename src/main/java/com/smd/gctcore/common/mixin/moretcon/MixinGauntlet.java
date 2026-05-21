package com.smd.gctcore.common.mixin.moretcon;

import com.existingeevee.moretcon.item.tooltypes.Gauntlet;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gauntlet.class)
public class MixinGauntlet {
    @Inject(method = "onLivingAttackEvent(Lnet/minecraftforge/event/entity/living/LivingKnockBackEvent;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelKnockBackEvent(LivingKnockBackEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onLivingAttackEvent(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelHurtEvent(LivingHurtEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
