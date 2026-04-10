package com.smd.gctcore.common.mixin.thaumicrestoration;

import com.Zoko061602.ThaumicRestoration.tile.TileCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileCrystal.class)
public class MixinTileCrystal {

    @Inject(method = "func_73660_a", at = @At("HEAD"), cancellable = true, remap = false)
    private void onUpdate(CallbackInfo ci) {
        ci.cancel();
    }
}
