package com.smd.gctcore.common.mixin.abyssalCraft;

import com.shinoow.abyssalcraft.common.items.ItemOC;
import com.smd.gctcore.common.config.GctCoreConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemOC.class)
public class MixinItemOC {
    @Inject(
            method = "onItemRightClick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectOblivionCatalyst(World world, EntityPlayer player, EnumHand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        ItemStack stack = player.getHeldItem(hand);

        if (GctCoreConfig.abyssalCraftIntegration.enableOblivionCatalystEffects) {
            cir.setReturnValue(new ActionResult<>(EnumActionResult.PASS, stack));
            return;
        }
    }
}

