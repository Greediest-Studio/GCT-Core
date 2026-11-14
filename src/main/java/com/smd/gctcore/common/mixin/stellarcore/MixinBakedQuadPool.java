package com.smd.gctcore.common.mixin.stellarcore;

import com.smd.gctcore.client.integration.stellarcore.StellarQuadPoolHelper;
import github.kasuminova.stellarcore.client.pool.BakedQuadPool;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents post-load SimpleBakedModel constructions from repopulating BakedQuadPool after loadComplete.
 */
@Mixin(value = BakedQuadPool.class, remap = false)
public abstract class MixinBakedQuadPool {

    @Inject(method = "canonicalizeAsync(Ljava/lang/Runnable;)V", at = @At("HEAD"), cancellable = true)
    private void gctcore$skipAsyncQuadPoolAfterReset(Runnable task, CallbackInfo ci) {
        if (StellarQuadPoolHelper.isPoolsFrozen()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "canonicalizeInternal(Lnet/minecraft/client/renderer/block/model/BakedQuad;)Lnet/minecraft/client/renderer/block/model/BakedQuad;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void gctcore$bypassFrozenQuadPool(BakedQuad quad, CallbackInfoReturnable<BakedQuad> cir) {
        if (StellarQuadPoolHelper.isPoolsFrozen()) {
            cir.setReturnValue(quad);
        }
    }
}
