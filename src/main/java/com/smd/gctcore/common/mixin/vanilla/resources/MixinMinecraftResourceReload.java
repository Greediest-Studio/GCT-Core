package com.smd.gctcore.common.mixin.vanilla.resources;

import com.smd.gctcore.client.PreInitResourceReloadCoalescer;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftResourceReload {

    @Inject(method = "refreshResources", at = @At("HEAD"), cancellable = true)
    private void gctcore$deferPreInitFullReload(CallbackInfo ci) {
        if (PreInitResourceReloadCoalescer.deferFullReload()) {
            ci.cancel();
        }
    }
}
