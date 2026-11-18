package com.smd.gctcore.common.mixin.vanilla.resources;

import com.smd.gctcore.client.PreInitResourceReloadCoalescer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = FMLClientHandler.class, remap = false)
public abstract class MixinFMLClientHandlerResourceReload {

    @Inject(
            method = "beginMinecraftLoading",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fml/common/Loader;preinitializeMods()V",
                    shift = At.Shift.BEFORE
            ),
            remap = false
    )
    private void gctcore$beginPreInitReloadScope(Minecraft minecraft, List<IResourcePack> resourcePackList,
                                                 IReloadableResourceManager resourceManager,
                                                 MetadataSerializer metaSerializer, CallbackInfo ci) {
        PreInitResourceReloadCoalescer.beginPreInit();
    }

    @Inject(
            method = "beginMinecraftLoading",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fml/common/Loader;preinitializeMods()V",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void gctcore$flushPreInitReloadScope(Minecraft minecraft, List<IResourcePack> resourcePackList,
                                                 IReloadableResourceManager resourceManager,
                                                 MetadataSerializer metaSerializer, CallbackInfo ci) {
        PreInitResourceReloadCoalescer.endPreInitAndFlush();
    }

    @Inject(method = "refreshResources(Ljava/util/function/Predicate;)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void gctcore$deferPreInitSelectiveReload(Predicate<IResourceType> filter, CallbackInfo ci) {
        if (PreInitResourceReloadCoalescer.deferSelectiveReload(filter)) {
            ci.cancel();
        }
    }
}
