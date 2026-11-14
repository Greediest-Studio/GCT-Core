package com.smd.gctcore.common.mixin.hei;

import com.smd.gctcore.common.integration.jei.HeiHashCacheHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.gui.textures.Textures;
import mezz.jei.startup.JeiStarter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Bounds HEI's ingredient hashCache lifetime to JeiStarter.load(...).
 */
@Mixin(value = JeiStarter.class, remap = false)
public abstract class MixinJeiStarter {

    @Inject(
            method = "load(Ljava/util/List;Lmezz/jei/gui/textures/Textures;Z)V",
            at = @At("HEAD"),
            remap = false
    )
    private void gctcore$onJeiLoadStart(List<IModPlugin> plugins, Textures textures, boolean isReload, CallbackInfo ci) {
        HeiHashCacheHelper.beginLoad();
    }

    @Inject(
            method = "load(Ljava/util/List;Lmezz/jei/gui/textures/Textures;Z)V",
            at = @At("RETURN"),
            remap = false
    )
    private void gctcore$onJeiLoadEnd(List<IModPlugin> plugins, Textures textures, boolean isReload, CallbackInfo ci) {
        HeiHashCacheHelper.endLoad();
    }
}
