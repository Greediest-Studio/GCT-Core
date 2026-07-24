package com.smd.gctcore.common.mixin.biomesoplenty;

import biomesoplenty.common.handler.FogEventHandler;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FogEventHandler.class, remap = false)
public class MixinFogEvent {

    /**
     * 在 onGetFogColor 头部直接返回，取消所有雾颜色处理
     */
    @Inject(method = "onGetFogColor", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelFogColor(EntityViewRenderEvent.FogColors event, CallbackInfo ci) {
        ci.cancel();
    }

    /**
     * 在 onRenderFog 头部直接返回，取消所有雾渲染距离处理
     */
    @Inject(method = "onRenderFog", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelRenderFog(EntityViewRenderEvent.RenderFogEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}