package com.smd.gctcore.common.mixin.mekanism;

import mekanism.qioprocessing.common.MekanismQIOProcessing;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips the eager global QIO workbench recipe catalog scan on server start so
 * large packs do not allocate the full QIO recipe catalog unless QIO is used.
 */
@Mixin(value = MekanismQIOProcessing.class, remap = false)
public abstract class MixinMekanismQIOProcessing {

    @Inject(
            method = "serverStarted(Lnet/minecraftforge/fml/common/event/FMLServerStartedEvent;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void gctcore$skipEagerQioCatalogScan(FMLServerStartedEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
