package com.smd.gctcore.common.mixin.bountifulbaubles;

import com.smd.gctcore.common.mixin.vanilla.entity.EntityAccessor;
import cursedflames.bountifulbaubles.item.ItemTrinketPotionCharm;
import cursedflames.bountifulbaubles.item.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemTrinketPotionCharm.class)
public abstract class MixinItemTrinketPotionCharm {

    @Inject(method = "onWornTick", at = @At("HEAD"), remap = false)
    private void onOnWornTick(ItemStack stack, EntityLivingBase player, CallbackInfo ci) {
        if (stack.getItem() == ModItems.trinketAnkhCharm && player instanceof EntityPlayer) {
            ((EntityAccessor) player).setIsInWeb(false);
        }
    }
}