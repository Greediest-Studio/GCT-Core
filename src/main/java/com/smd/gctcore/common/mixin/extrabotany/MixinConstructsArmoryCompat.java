package com.smd.gctcore.common.mixin.extrabotany;

import com.meteor.extrabotany.common.integration.constructsarmory.ConstructsArmoryCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ports Extra-Botany "禁用匠魂材料注册" armor side: skip Orichalcos/Shadowium ConArm stats.
 */
@Mixin(value = ConstructsArmoryCompat.class, remap = false)
public class MixinConstructsArmoryCompat {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private static void gct$disableArmorMaterialStats(CallbackInfo ci) {
        ci.cancel();
    }
}
