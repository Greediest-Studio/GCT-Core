package com.smd.gctcore.common.mixin.nuclearcraft;

import com.smd.gctcore.gctcore;
import com.smd.gctcore.common.config.GCTMixinConfig;
import com.smd.gctcore.common.optimization.ContentTweakerResourceMerge;
import nc.handler.ScriptAddonHandler;
import org.apache.commons.io.FileUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.IOException;

@Mixin(value = ScriptAddonHandler.class, remap = false)
public abstract class MixinScriptAddonHandler {
    @Unique private static ContentTweakerResourceMerge gctcore$resourceMerge;
    @Unique private static long gctcore$constructStart;

    @Inject(method = "construct", at = @At("HEAD"), require = 1)
    private static void gctcore$beginMerge(CallbackInfo ci) throws IOException {
        gctcore$constructStart = System.nanoTime();
        gctcore$resourceMerge = null;
        if (GCTMixinConfig.enableNuclearCraftResourceMerge) {
            gctcore$resourceMerge = ContentTweakerResourceMerge.create(
                    new File("resources/contenttweaker"), new File("resources/.contenttweaker"));
        }
    }

    @Redirect(method = "copyAddons", at = @At(value = "INVOKE",
            target = "Lorg/apache/commons/io/FileUtils;copyDirectory(Ljava/io/File;Ljava/io/File;)V"), require = 1)
    private static void gctcore$mergeResources(File source, File destination) throws IOException {
        if (gctcore$resourceMerge == null) FileUtils.copyDirectory(source, destination);
        else gctcore$resourceMerge.copyDirectory(source, destination);
    }

    @Inject(method = "construct", at = @At("RETURN"), require = 1)
    private static void gctcore$finishMerge(CallbackInfo ci) {
        ContentTweakerResourceMerge merge = gctcore$resourceMerge;
        gctcore$resourceMerge = null;
        if (merge != null) {
            gctcore.LOGGER.info("[GCTCore-NuclearCraft] Constructed addons in {}ms; preserved={}, copied={}, skipped={}.",
                    (System.nanoTime() - gctcore$constructStart) / 1_000_000,
                    merge.getInitialFileCount(), merge.getCopiedFiles(), merge.getSkippedFiles());
        }
    }
}
