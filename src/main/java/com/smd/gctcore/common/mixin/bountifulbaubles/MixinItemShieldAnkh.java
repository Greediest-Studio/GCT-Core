package com.smd.gctcore.common.mixin.bountifulbaubles;

import com.smd.gctcore.common.util.EntityWebState;
import cursedflames.bountifulbaubles.item.ItemShieldAnkh;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemShieldAnkh.class)
public class MixinItemShieldAnkh {

    @Inject(method = "onWornTick", at = @At("HEAD"), remap = false)
    private void onOnWornTick(ItemStack stack, EntityLivingBase player, CallbackInfo ci) {
        if (player instanceof EntityPlayer) {
            EntityWebState.clear(player);
        }
    }
}
