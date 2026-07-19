package com.smd.gctcore.common.mixin.extrabotany;

import com.meteor.extrabotany.common.integration.tinkerconstruct.Body;
import com.meteor.extrabotany.common.integration.tinkerconstruct.Mana;
import com.meteor.extrabotany.common.integration.tinkerconstruct.Mind;
import com.meteor.extrabotany.common.integration.tinkerconstruct.Shadow;
import com.meteor.extrabotany.common.integration.tinkerconstruct.Soul;
import com.meteor.extrabotany.common.integration.tinkerconstruct.TConstructCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.TinkerRegistry;

/**
 * Ports Extra-Botany "禁用匠魂材料注册": keep traits, skip Shadowium/Orichalcos material registration.
 */
@Mixin(value = TConstructCompat.class, remap = false)
public class MixinTConstructCompat {

    @Inject(method = "preInit", at = @At("HEAD"), cancellable = true)
    private static void gct$disableMaterialRegistration(CallbackInfo ci) {
        // Traits remain available for CraftTweaker / other materials
        TinkerRegistry.addTrait(Body.body);
        TinkerRegistry.addTrait(Mind.mind);
        TinkerRegistry.addTrait(Soul.soul);
        TinkerRegistry.addTrait(Shadow.shadow);
        TinkerRegistry.addTrait(Mana.mana);
        // Skip register() + preIntegrate — materials stay unregistered
        ci.cancel();
    }
}
