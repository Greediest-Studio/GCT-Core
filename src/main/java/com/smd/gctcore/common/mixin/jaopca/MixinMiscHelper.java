package com.smd.gctcore.common.mixin.jaopca;

import com.smd.gctcore.client.JAOPCAResourceCache;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "thelm.jaopca.utils.MiscHelper", remap = false)
public abstract class MixinMiscHelper {

    @Inject(method = "hasResource(Lnet/minecraft/util/ResourceLocation;)Z", at = @At("HEAD"),
            cancellable = true, remap = false, require = 1)
    private void gctcore$cacheResourceExists(ResourceLocation location, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(JAOPCAResourceCache.hasResource(location));
    }
}
