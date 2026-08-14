package com.smd.gctcore.common.mixin.fluxapplied;

import appeng.api.util.AECableType;
import appeng.parts.AEBasePart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AEBasePart.class)
public abstract class MixinAEBasePart {

    private static final String ENERGY_STORAGE_BUS = "com.flux_applied.part.PartEnergyStorageBus";
    private static final String ENERGY_PROVIDER = "com.flux_applied.part.PartEnergyProvider";

    @Inject(method = "getCableConnectionLength", at = @At("HEAD"), cancellable = true, remap = false)
    private void gctcore$fixFluxPartConnectionLength(
            AECableType cable,
            CallbackInfoReturnable<Float> cir
    ) {
        String className = ((Object) this).getClass().getName();
        if (ENERGY_STORAGE_BUS.equals(className) || ENERGY_PROVIDER.equals(className)) {
            cir.setReturnValue(4.0F);
        }
    }
}
