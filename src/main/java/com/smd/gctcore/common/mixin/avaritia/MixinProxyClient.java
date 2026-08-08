package com.smd.gctcore.common.mixin.avaritia;

import morph.avaritia.proxy.ProxyClient;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ProxyClient.class, remap = false)
public abstract class MixinProxyClient {

    @Inject(
            method = "lambda$postInit$12",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void gctcore$avoidSingularityColourOverflow(
            ItemStack stack,
            int tintIndex,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (stack.getMetadata() >= 11) {
            cir.setReturnValue(-1);
        }
    }
}
