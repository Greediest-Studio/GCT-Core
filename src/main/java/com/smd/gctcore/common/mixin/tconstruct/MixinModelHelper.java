package com.smd.gctcore.common.mixin.tconstruct;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.library.client.model.ModelHelper;

import java.io.IOException;
import java.io.Reader;

@Mixin(ModelHelper.class)
public class MixinModelHelper {

    @Inject(
            method = "getReaderForResource(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/resources/IResourceManager;)Ljava/io/Reader;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void suppressMissingModifierModels(ResourceLocation location,
                                                      IResourceManager resourceManager,
                                                      CallbackInfoReturnable<Reader> cir) {

        ResourceLocation file = new ResourceLocation(location.getNamespace(), location.getPath() + ".json");

        if (file.getPath().contains("modifiers/")) {
            try {
                resourceManager.getResource(file);
            } catch (IOException e) {
                cir.setReturnValue(null);
            }
        }
    }
}