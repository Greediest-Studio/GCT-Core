package com.smd.gctcore.common.mixin.ae2;

import appeng.container.ContainerNull;
import appeng.container.slot.AppEngCraftingSlot;
import com.smd.gctcore.common.integration.ae2.CraftingTerminalCache;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = AppEngCraftingSlot.class, remap = false)
public abstract class MixinAppEngCraftingSlot {

    @ModifyArg(
            method = "onTake",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/InventoryCrafting;<init>(Lnet/minecraft/inventory/Container;II)V", remap = true),
            index = 0,
            remap = true,
            require = 1
    )
    private Container gctcore$useSilentCraftingMatrix(Container eventHandler) {
        // 临时矩阵逐格复制时不刷新真实终端；craftItem 在扣料、补料后已有完整刷新。
        return eventHandler instanceof CraftingTerminalCache ? new ContainerNull() : eventHandler;
    }
}
