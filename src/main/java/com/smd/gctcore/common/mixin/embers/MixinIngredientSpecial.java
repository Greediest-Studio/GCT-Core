package com.smd.gctcore.common.mixin.embers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

/**
 * Embers assumes every registered item has a creative-tab array.  Forge allows
 * items to return {@code null} here, however, and the assumption causes recipe
 * registration to crash while IngredientSpecial caches its matching stacks.
 */
@Pseudo
@Mixin(targets = "teamroots.embers.util.IngredientSpecial", remap = false)
public abstract class MixinIngredientSpecial {

    @Inject(
            method = "lambda$cacheMatchingStacks$0",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private static void gctcore$skipItemWithoutCreativeTabs(
            Item item,
            CallbackInfoReturnable<Stream<ItemStack>> cir
    ) {
        if (item != null && item.getCreativeTabs() == null) {
            cir.setReturnValue(Stream.empty());
        }
    }
}
