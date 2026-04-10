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
                    target = "Lnet/minecraft/client/resources/IResourceManager;getResource(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/resources/IResource;",
                    remap = true
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            require = 1,
            cancellable = true
    )
    private static void suppressMissingModifierModels(
            ResourceLocation location,
            IResourceManager resourceManager,
            CallbackInfoReturnable<Reader> cir,
            ResourceLocation file
    ) {
        // 检查是否是修饰符模型文件
        if (file.getPath().contains("modifiers/")) {
            try {
                // 尝试获取资源
                resourceManager.getResource(file);
            } catch (IOException e) {
                // 忽略修饰符模型文件不存在的异常
                cir.setReturnValue(null);
            }
        }
    }
}
