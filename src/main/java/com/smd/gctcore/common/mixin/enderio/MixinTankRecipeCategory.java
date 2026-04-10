package com.smd.gctcore.common.mixin.enderio;

import crazypants.enderio.machines.integration.jei.TankRecipeCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TankRecipeCategory.class, remap = false)
public abstract class MixinTankRecipeCategory {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gctcore$disableTankJeiCategory(CallbackInfo ci) {
        ci.cancel();
    }
}
