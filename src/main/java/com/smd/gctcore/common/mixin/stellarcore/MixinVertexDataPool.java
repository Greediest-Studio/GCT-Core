package com.smd.gctcore.common.mixin.stellarcore;

import com.smd.gctcore.client.integration.stellarcore.StellarQuadPoolHelper;
import github.kasuminova.stellarcore.client.pool.StellarUnpackedDataPool;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Guards VertexDataPool worker/sync lookups after reset() and trims its backing table when cleared.
 */
@Mixin(value = StellarUnpackedDataPool.VertexDataPool.class, remap = false)
public abstract class MixinVertexDataPool {

    @Shadow
    private ObjectOpenCustomHashSet<int[]> pool;

    @Inject(
            method = "canonicalizeInternal([I)[I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gctcore$bypassFrozenVertexPool(int[] value, CallbackInfoReturnable<int[]> cir) {
        if (StellarQuadPoolHelper.isPoolsFrozen()) {
            cir.setReturnValue(value);
        }
    }

    @Inject(method = "clearPool()V", at = @At("RETURN"))
    private void gctcore$trimVertexPoolOnClear(CallbackInfo ci) {
        if (this.pool != null) {
            this.pool.trim();
        }
    }
}
