package com.smd.gctcore.common.mixin.vanilla.resources;

import com.smd.gctcore.client.JAOPCAResourceCache;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleReloadableResourceManager.class)
public abstract class MixinSimpleReloadableResourceManager {

    @Inject(method = "reloadResources", at = @At("HEAD"), require = 1)
    private void gctcore$clearJAOPCAResourceCache(CallbackInfo ci) {
        // 在纹理重载监听器运行前清空，避免切换资源包时沿用旧结果。
        JAOPCAResourceCache.clear();
    }
}
