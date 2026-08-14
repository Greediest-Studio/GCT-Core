package com.smd.gctcore.common.mixin.fluxapplied;

import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.flux_applied.part.PartEnergyStorageBus", remap = false)
public abstract class MixinPartEnergyStorageBus {

    @Inject(method = "getCableConnectionType", at = @At("HEAD"), cancellable = true, remap = false)
    private void gctcore$useGlassCableConnection(
            AEPartLocation location,
            CallbackInfoReturnable<AECableType> cir
    ) {
        cir.setReturnValue(AECableType.GLASS);
    }
}
