package com.smd.gctcore.common.mixin.moretcon;

import com.existingeevee.moretcon.traits.traits.armor.Gravitating;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gravitating.class)
public class MixinGravitating {

    @Inject(method = "onLivingUpdateEvent", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelUpdate(LivingUpdateEvent e, CallbackInfo ci) {
        ci.cancel();
    }

}