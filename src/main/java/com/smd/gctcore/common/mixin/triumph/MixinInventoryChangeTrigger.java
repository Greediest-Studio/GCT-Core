package com.smd.gctcore.common.mixin.triumph;

import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryChangeTrigger.class)
public class MixinInventoryChangeTrigger {

    /**
     * 修复Triumph模组触发InventoryChangeTrigger时可能传入null ItemStack导致的NPE
     * 当stack为null时，直接返回true（视为空物品栈）
     */
    @Redirect(
            method = "trigger(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/entity/player/InventoryPlayer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean gctcore$safeIsEmpty(ItemStack stack) {
        return stack == null || stack.isEmpty();
    }
}
