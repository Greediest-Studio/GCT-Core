package com.smd.gctcore.common.mixin.scalingguis;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import spazley.scalingguis.handlers.ClientEventHandler;
import spazley.scalingguis.handlers.ConfigHandler;

@Mixin(value = ClientEventHandler.class, remap = false)
public abstract class MixinClientEventHandler {

    @Inject(
            method = "onPreRenderGameOverlay",
            at = @At("HEAD"),
            cancellable = true
    )
    private void scalingguis$skipIfScaleUnchanged(RenderGameOverlayEvent.Pre e, CallbackInfo ci) {

        if (e.getType() != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        int newScale = ConfigHandler.getHudScale();
        int oldScale = Minecraft.getMinecraft().gameSettings.guiScale;

        if (newScale == oldScale) {
            ci.cancel();
        }
    }
}
