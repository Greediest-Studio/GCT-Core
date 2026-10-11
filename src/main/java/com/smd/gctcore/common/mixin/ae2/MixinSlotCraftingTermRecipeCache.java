package com.smd.gctcore.common.mixin.ae2;

import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.slot.SlotCraftingTerm;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import com.smd.gctcore.common.integration.ae2.CraftingTerminalCache;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SlotCraftingTerm.class, remap = false)
public abstract class MixinSlotCraftingTermRecipeCache {

    @Shadow(remap = false) @Final private IContainerCraftingPacket container;
    @Unique private boolean gctcore$lastCraftFailed;

    @Shadow(remap = false)
    private IRecipe handleRecipe(InventoryCrafting matrix, IRecipe recipe, EntityPlayer player) {
        throw new AssertionError();
    }

    @Inject(method = "findRecipe", at = @At("HEAD"), cancellable = true, require = 1)
    private void gctcore$reuseRecipe(InventoryCrafting matrix, World world, EntityPlayer player,
                                   CallbackInfoReturnable<IRecipe> cir) {
        if (container instanceof CraftingTerminalCache) {
            IRecipe recipe = ((CraftingTerminalCache) container).gctcore$getCachedRecipe();
            if (recipe != null && recipe.matches(matrix, world)) {
                // 无线终端也复用当前配方，同时保留 AE2 的 RecipeStages 校验。
                cir.setReturnValue(handleRecipe(matrix, recipe, player));
            }
        }
    }

    @Inject(method = "getRemainingItems", at = @At("HEAD"), cancellable = true, require = 1)
    private void gctcore$reuseRecipeForRemainders(InventoryCrafting matrix, World world,
                                                CallbackInfoReturnable<NonNullList<ItemStack>> cir) {
        if (container instanceof CraftingTerminalCache) {
            IRecipe recipe = ((CraftingTerminalCache) container).gctcore$getCachedRecipe();
            if (recipe != null && recipe.matches(matrix, world)) {
                cir.setReturnValue(recipe.getRemainingItems(matrix));
            }
        }
    }

    @Inject(method = "doClick", at = @At("HEAD"), require = 1)
    private void gctcore$resetCraftFailure(InventoryAction action, EntityPlayer player, CallbackInfo ci) {
        gctcore$lastCraftFailed = false;
    }

    @Inject(method = "craftItem", at = @At("RETURN"), require = 1)
    private void gctcore$rememberCraftFailure(EntityPlayer player, ItemStack request,
                                            IMEMonitor<IAEItemStack> inventory, IItemList<?> items,
                                            CallbackInfoReturnable<ItemStack> cir) {
        gctcore$lastCraftFailed = cir.getReturnValue().isEmpty();
    }

    @Inject(
            method = "doClick",
            at = @At(value = "INVOKE", target = "Lappeng/container/slot/SlotCraftingTerm;craftItem(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;Lappeng/api/storage/IMEMonitor;Lappeng/api/storage/data/IItemList;)Lnet/minecraft/item/ItemStack;"),
            cancellable = true,
            require = 1
    )
    private void gctcore$stopFailedBatch(InventoryAction action, EntityPlayer player, CallbackInfo ci) {
        // 材料耗尽后不继续空转剩余次数；外层 finally 仍会完成刷新和保存。
        if (container instanceof CraftingTerminalCache && gctcore$lastCraftFailed) {
            ci.cancel();
        }
    }
}
