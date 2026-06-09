package com.smd.gctcore.common.mixin.jei;

import mezz.jei.util.StringUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StringUtil.class, remap = false)
public class MixinStringUtil {

    @Inject(method = "intern", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gctcore$useJvmStringInterner(String string, CallbackInfoReturnable<String> cir) {
        if (string == null) {
            cir.setReturnValue(null);
            return;
        }

        cir.setReturnValue(string.intern());
    }
}
