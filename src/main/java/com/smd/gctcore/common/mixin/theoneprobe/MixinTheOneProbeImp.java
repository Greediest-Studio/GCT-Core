package com.smd.gctcore.common.mixin.theoneprobe;

import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.apiimpl.TheOneProbeImp;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mixin(value = TheOneProbeImp.class, remap = false)
public abstract class MixinTheOneProbeImp {

    @Unique
    private static final Set<String> GCTCORE_SUPERSEDED_BOTANIA_PROVIDERS = new HashSet<>(Arrays.asList(
            "botania.pool",
            "botania.Spreader",
            "botaniverse.pool",
            "botaniverse.Spreader",
            "botanic_additions.DreamingManapool",
            "extrabotany.pool",
            "extrabotany.spreader"
    ));

    @Inject(method = "registerProvider", at = @At("HEAD"), cancellable = true)
    private void gctcore$disableSupersededBotaniaProviders(IProbeInfoProvider provider, CallbackInfo ci) {
        if (GCTCORE_SUPERSEDED_BOTANIA_PROVIDERS.contains(provider.getID())) {
            ci.cancel();
        }
    }
}
