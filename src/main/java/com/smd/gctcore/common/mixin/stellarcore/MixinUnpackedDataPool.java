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
 * Guards UnpackedDataPool worker execution against inserting into the set after reset()
 * and trims the underlying OpenCustomHashSet table when cleared.
 */
@Mixin(value = StellarUnpackedDataPool.UnpackedDataPool.class, remap = false)
public abstract class MixinUnpackedDataPool<T> {

    @Shadow
    private ObjectOpenCustomHashSet<T> pool;

    @Inject(
            method = "canonicalizeInternal(Ljava/lang/Object;)Ljava/lang/Object;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gctcore$bypassFrozenUnpackedPool(T value, CallbackInfoReturnable<T> cir) {
        if (StellarQuadPoolHelper.isPoolsFrozen()) {
            cir.setReturnValue(value);
        }
    }

    @Inject(method = "clearPool()V", at = @At("RETURN"))
    private void gctcore$trimOnClear(CallbackInfo ci) {
        if (this.pool != null) {
            this.pool.trim();
        }
    }
}
