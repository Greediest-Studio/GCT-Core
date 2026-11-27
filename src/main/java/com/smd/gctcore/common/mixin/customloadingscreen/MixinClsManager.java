package com.smd.gctcore.common.mixin.customloadingscreen;

import com.smd.gctcore.gctcore;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.SelectiveReloadStateHandler;
import net.minecraftforge.client.resource.VanillaResourceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

/**
 * CustomLoadingScreen 在 FMLConstructionEvent 中调用 ClsManager.load() 时会执行 mc.refreshResources()，
 * 以使 SimpleReloadableResourceManager 挂载自定义加载界面资源，但会附带触发全模组 LanguageManager 语言文件重载。
 * CLS 自身使用独立 Translation 类且不依赖原版 LanguageManager，后续 FMLClientHandler.beginMinecraftLoading()
 * 还会完整重载一次语言文件；因此在本次刷新中排除 LANGUAGES 监听器即可保留全部加载界面资源并省去冗余语言解析。
 */
@Pseudo
@Mixin(targets = "alexiil.mc.mod.load.ClsManager", remap = false)
public abstract class MixinClsManager {

    @Unique
    private static final Predicate<IResourceType> gctcore$SKIP_LANGUAGES =
            type -> type != VanillaResourceType.LANGUAGES;

    @Redirect(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;refreshResources()V",
                    remap = true
            ),
            require = 0
    )
    private static void gctcore$refreshWithoutLanguages(Minecraft mc) {
        gctcore.LOGGER.info("[GCTCore-CLS] Refreshing resource packs for CustomLoadingScreen without LanguageManager reload.");
        SelectiveReloadStateHandler.INSTANCE.beginReload(gctcore$SKIP_LANGUAGES);
        try {
            mc.refreshResources();
        } finally {
            SelectiveReloadStateHandler.INSTANCE.endReload();
        }
    }
}
