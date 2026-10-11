package com.smd.gctcore.common.mixin.ae2;

import appeng.container.implementations.ContainerMEMonitorable;
import appeng.container.implementations.ContainerMEPortableTerminal;
import com.smd.gctcore.common.integration.ae2.CraftingTerminalCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {ContainerMEMonitorable.class, ContainerMEPortableTerminal.class}, remap = false)
public abstract class MixinTerminalBatchSynchronization {

    @Inject(method = "detectAndSendChanges", at = @At("HEAD"), cancellable = true, remap = true, require = 1)
    private void gctcore$deferBatchSynchronization(CallbackInfo ci) {
        // 在 AE2 容器入口合并同步，不依赖多态合成内部服务的方法签名。
        // 普通和无线合成终端都在批次 finally 中恢复同步，其他终端不受影响。
        if ((Object) this instanceof CraftingTerminalCache
                && ((CraftingTerminalCache) this).gctcore$isBatchCrafting()) {
            ci.cancel();
        }
    }
}
