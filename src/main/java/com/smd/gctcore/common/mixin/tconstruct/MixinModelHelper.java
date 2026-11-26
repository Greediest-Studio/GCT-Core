package com.smd.gctcore.common.mixin.tconstruct;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.library.client.model.ModifierModelLoader;

import java.io.IOException;

@Mixin(value = ModifierModelLoader.class, remap = false)
public abstract class MixinModelHelper {

    @Redirect(
            method = "loadFilesIntoCache()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V",
                    remap = false
            ),
            remap = false
    )
    private void gctcore$suppressMissingModifierModelLog(Logger logger, String message, Throwable throwable) {
        if (throwable instanceof IOException) {
            logger.debug(message);
            return;
        }
        logger.error(message, throwable);
    }
}