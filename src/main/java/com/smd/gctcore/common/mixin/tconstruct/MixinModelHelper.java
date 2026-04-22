package com.smd.gctcore.common.mixin.tconstruct;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import slimeknights.tconstruct.library.client.model.ModelHelper;

import java.io.IOException;
import java.io.Reader;

@Mixin(ModelHelper.class)
public class MixinModelHelper {

    @Inject(
            method = "getReaderForResource(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/resources/IResourceManager;)Ljava/io/Reader;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/IResourceManager;func_110536_a(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/resources/IResource;",
                    remap = false
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true,
            remap = false
    )
    private static void suppressMissingModifierModels(ResourceLocation location,
                                                      IResourceManager resourceManager,
                                                      CallbackInfoReturnable<Reader> cir,
                                                      ResourceLocation file) {

        if (file.getPath().contains("modifiers/")) {
            try {
                resourceManager.getResource(file);
            } catch (IOException e) {
                cir.setReturnValue(null);
            }
        }
    }
}