package com.smd.gctcore.common.mixin.enderio;

import crazypants.enderio.base.diagnostics.ModInterferenceWarner;
import crazypants.enderio.base.events.EnderIOLifecycleEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModInterferenceWarner.class, remap = false)
public abstract class MixinModInterferenceWarner {
    @Inject(method = "onEvent", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gctcore$disableInterferenceWarning(EnderIOLifecycleEvent.ServerAboutToStart.Pre event, CallbackInfo ci) {
        ci.cancel();
    }
}
