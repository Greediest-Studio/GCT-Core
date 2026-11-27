package com.smd.gctcore.common.mixin.nuclearcraft;

import com.smd.gctcore.common.config.GCTMixinConfig;
import com.smd.gctcore.gctcore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NuclearCraft Overhauled (2o.9.x+) 在 CommonProxy.postInit() 中无条件调用 NCPFWriter.exportNCPF()，
 * 每次启动都会序列化并去重写入 config/nuclearcraft.ncpf.json（约耗时 1.1s）。
 * 该文件仅供外部规划器使用，游戏内不会读取；此处默认跳过，需要导出时可关闭配置或传 -Dgctcore.exportNcpf=true。
 */
@Pseudo
@Mixin(targets = "nc.ncpf.NCPFWriter", remap = false)
public abstract class MixinNCPFWriter {

    @Inject(method = "exportNCPF", at = @At("HEAD"), cancellable = true, require = 0)
    private static void gctcore$skipExportNCPF(CallbackInfo ci) {
        if (GCTMixinConfig.disableNuclearCraftNCPFExport && !Boolean.getBoolean("gctcore.exportNcpf")) {
            gctcore.LOGGER.info("[GCTCore-NuclearCraft] Skipped startup NCPF export (set -Dgctcore.exportNcpf=true to force).");
            ci.cancel();
        }
    }
}
