package com.smd.gctcore.common.mixin.customloadingscreen;

import com.smd.gctcore.gctcore;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * ClsManager.load() 在构造 MinecraftDisplayerRenderer 前刚刚执行过一次 mc.refreshResources()，
 * 期间没有任何新资源包加入，MinecraftDisplayerRenderer 构造函数中的第二次 mc.refreshResources() 完全冗余。
 */
@Pseudo
@Mixin(targets = "alexiil.mc.mod.load.render.MinecraftDisplayerRenderer", remap = false)
public abstract class MixinMinecraftDisplayerRenderer {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;refreshResources()V",
                    remap = true
            ),
            require = 0
    )
    private void gctcore$skipDuplicateRefresh(Minecraft mc) {
        gctcore.LOGGER.info("[GCTCore-CLS] Skipped duplicate Minecraft.refreshResources() in MinecraftDisplayerRenderer.<init>.");
    }
}
