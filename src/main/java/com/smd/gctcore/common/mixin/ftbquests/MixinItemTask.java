package com.smd.gctcore.common.mixin.ftbquests;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "com.feed_the_beast.ftbquests.quest.task.ItemTask", remap = false)
public abstract class MixinItemTask {

    private static final String RETRO_BACKPACK_ITEM =
            "com.cleanroommc.retrosophisticatedbackpacks.item.BackpackItem";

    @Shadow(remap = false)
    @Final
    public List<ItemStack> items;

    @Inject(
            method = "test(Lnet/minecraft/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void gctcore$matchRetroBackpackByItem(
            ItemStack stack,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (stack.isEmpty() || !isRetroBackpack(stack)) {
            return;
        }

        for (ItemStack filter : items) {
            if (!filter.isEmpty() && filter.getItem() == stack.getItem()) {
                cir.setReturnValue(true);
                return;
            }
        }

        cir.setReturnValue(false);
    }

    private static boolean isRetroBackpack(ItemStack stack) {
        return stack.getItem().getClass().getName().equals(RETRO_BACKPACK_ITEM);
    }
}
