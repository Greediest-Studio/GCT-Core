package com.smd.gctcore.common.mixin.enderstorage;

import codechicken.enderstorage.network.TankSynchroniser;
import codechicken.lib.packet.PacketCustom;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Mixin(value = TankSynchroniser.class, remap = false)
public abstract class MixinTankSynchroniser {

    @Shadow private static HashMap<String, TankSynchroniser.PlayerItemTankCache> playerItemTankStates;

    @Inject(method = "handleVisiblityPacket", at = @At("HEAD"), cancellable = true, require = 1)
    private static void gctcore$skipVisibilityWithoutCache(EntityPlayerMP player, PacketCustom packet, CallbackInfo ci) {
        if (playerItemTankStates == null || playerItemTankStates.get(player.getName()) == null) {
            ci.cancel();
        }
    }
}
