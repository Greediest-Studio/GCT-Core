package com.smd.gctcore.common.mixin.ae2polymorph;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotCraftingTerm;
import com.smd.gctcore.common.integration.ae2.CraftingTerminalCache;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.shiver.polymorphlegacy.compat.ae2.Ae2CraftingContext", remap = false)
public abstract class MixinAe2CraftingContext {

    @Shadow(remap = false) @Final private AEBaseContainer terminal;
    @Shadow(remap = false) @Final private SlotCraftingTerm outputSlot;

    @Unique
    private boolean gctcore$isBatchCrafting() {
        return terminal instanceof CraftingTerminalCache
                && ((CraftingTerminalCache) terminal).gctcore$isBatchCrafting();
    }

    @Inject(method = "detectChanges", at = @At("HEAD"), cancellable = true, require = 1)
    private void gctcore$deferBatchDetection(CallbackInfo ci) {
        // 批次开始已解析，之后每次完成合成都通过 refresh 更新，结束再完整同步。
        if (gctcore$isBatchCrafting()) {
            ci.cancel();
        }
    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true, require = 1)
    private void gctcore$reuseSelectedRecipe(InventoryCrafting matrix, CallbackInfo ci) {
        if (!gctcore$isBatchCrafting()) {
            return;
        }

        IRecipe recipe = ((CraftingTerminalCache) terminal).gctcore$getCachedRecipe();
        if (recipe == null || !recipe.matches(matrix, terminal.getPlayerInv().player.world)) {
            return;
        }
        ItemStack output = recipe.getCraftingResult(matrix);
        if (output.isEmpty()) {
            return;
        }

        // 仅复用当前选择，产物每次重算。失效时走原方法重新枚举并更新多态状态。
        // 不覆盖 choices/selection，批次结束会按最终输入完整刷新候选列表。
        outputSlot.putStack(output);
        ci.cancel();
    }
}
