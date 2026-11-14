package com.smd.gctcore.common.mixin.stellarcore;

import com.smd.gctcore.client.integration.stellarcore.StellarQuadPoolHelper;
import github.kasuminova.stellarcore.client.pool.StellarUnpackedDataPool;
import github.kasuminova.stellarcore.common.pool.DeferredCanonicalizable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

/**
 * Stops StellarUnpackedDataPool from accepting new arrays after load reset().
 */
@Mixin(value = StellarUnpackedDataPool.class, remap = false)
public abstract class MixinStellarUnpackedDataPool {

    @Inject(method = "reset()V", at = @At("HEAD"))
    private static void gctcore$freezeOnReset(CallbackInfo ci) {
        StellarQuadPoolHelper.freezePools();
    }

    @Inject(
            method = "canonicalizeAsync([[[FLjava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gctcore$skipAsyncUnpackedAfterReset(float[][][] unpackedData,
                                                             Consumer<float[][][]> callback,
                                                             CallbackInfo ci) {
        if (StellarQuadPoolHelper.isPoolsFrozen()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "canonicalizeDeferred(Lgithub/kasuminova/stellarcore/common/pool/DeferredCanonicalizable;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gctcore$skipDeferredUnpackedAfterReset(DeferredCanonicalizable<float[][][]> target,
                                                                CallbackInfo ci) {
        if (StellarQuadPoolHelper.isPoolsFrozen()) {
            ci.cancel();
        }
    }

    @Inject(method = "canonicalize([[[F)[[[F", at = @At("HEAD"), cancellable = true)
    private static void gctcore$skipSyncUnpackedAfterReset(float[][][] unpackedData,
                                                            CallbackInfoReturnable<float[][][]> cir) {
        if (StellarQuadPoolHelper.isPoolsFrozen()) {
            cir.setReturnValue(unpackedData);
        }
    }

    @Inject(
            method = "canonicalizeAsync([ILjava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void gctcore$skipAsyncVertexAfterReset(int[] vertexData,
                                                           Consumer<int[]> callback,
                                                           CallbackInfo ci) {
        if (StellarQuadPoolHelper.isPoolsFrozen()) {
            ci.cancel();
        }
    }

    @Inject(method = "canonicalize([I)[I", at = @At("HEAD"), cancellable = true)
    private static void gctcore$skipSyncVertexAfterReset(int[] vertexData,
                                                          CallbackInfoReturnable<int[]> cir) {
        if (StellarQuadPoolHelper.isPoolsFrozen()) {
            cir.setReturnValue(vertexData);
        }
    }
}
