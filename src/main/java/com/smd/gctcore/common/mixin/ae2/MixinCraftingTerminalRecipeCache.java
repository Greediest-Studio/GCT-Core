package com.smd.gctcore.common.mixin.ae2;

import appeng.container.implementations.ContainerCraftingTerm;
import appeng.container.implementations.ContainerWirelessCraftingTerminal;
import com.smd.gctcore.common.integration.ae2.CraftingTerminalCache;
import net.minecraft.item.crafting.IRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {ContainerCraftingTerm.class, ContainerWirelessCraftingTerminal.class}, remap = false)
public abstract class MixinCraftingTerminalRecipeCache implements CraftingTerminalCache {

    // 多目标 Mixin 的运行时校验要求在 @Shadow 上显式关闭重映射。
    @Shadow(remap = false) private IRecipe currentRecipe;
    @Unique private boolean gctcore$batchCrafting;

    @Override
    public IRecipe gctcore$getCachedRecipe() {
        return currentRecipe;
    }

    @Override
    public boolean gctcore$isBatchCrafting() {
        return gctcore$batchCrafting;
    }

    @Override
    public void gctcore$setBatchCrafting(boolean crafting) {
        gctcore$batchCrafting = crafting;
    }

    @Inject(method = "saveChanges", at = @At("HEAD"), cancellable = true, require = 1)
    private void gctcore$deferBatchSave(CallbackInfo ci) {
        // 无线终端每个材料槽变化都会序列化背包；批次结束的刷新会统一保存。
        if (gctcore$batchCrafting) {
            ci.cancel();
        }
    }
}
