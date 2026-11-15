package com.smd.gctcore.common.mixin.thaumicconcilium;

import com.smd.gctcore.common.events.PlayerCacheCleanup;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.ConcurrentHashMap;

@Pseudo
@Mixin(targets = "com.keletu.thaumicconcilium.events.EventHandlerEntity", remap = false)
public abstract class MixinEventHandlerEntity {

    @Shadow @Final public static ConcurrentHashMap<EntityPlayer, Boolean> etherealsClient;
    @Shadow @Final public static ConcurrentHashMap<EntityPlayer, Boolean> etherealsServer;

    @Inject(method = "<clinit>", at = @At("RETURN"), require = 1)
    private static void gctcore$registerCacheCleanup(CallbackInfo ci) {
        PlayerCacheCleanup.register(etherealsClient);
        PlayerCacheCleanup.register(etherealsServer);
    }
}
