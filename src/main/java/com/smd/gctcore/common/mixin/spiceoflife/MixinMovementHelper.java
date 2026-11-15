package com.smd.gctcore.common.mixin.spiceoflife;

import com.smd.gctcore.common.events.PlayerCacheCleanup;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

@Pseudo
@Mixin(targets = "squeek.spiceoflife.helpers.MovementHelper", remap = false)
public abstract class MixinMovementHelper {

    @Shadow public static HashMap<EntityPlayer, ?> movementInfoByPlayer;

    @Inject(method = "<clinit>", at = @At("RETURN"), require = 1)
    private static void gctcore$registerCacheCleanup(CallbackInfo ci) {
        PlayerCacheCleanup.register(movementInfoByPlayer);
    }

    @Redirect(method = {"getDidJumpLastTick", "onLivingJump"}, at = @At(value = "INVOKE",
            target = "Ljava/util/HashMap;get(Ljava/lang/Object;)Ljava/lang/Object;"), require = 2)
    private static Object gctcore$getMovementInfo(HashMap<?, ?> players, Object player) {
        synchronized (players) {
            return players.get(player);
        }
    }

    @Redirect(method = "onLivingJump", at = @At(value = "INVOKE",
            target = "Ljava/util/HashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), require = 1)
    private Object gctcore$putMovementInfo(HashMap<Object, Object> players, Object player, Object info) {
        synchronized (players) {
            return players.put(player, info);
        }
    }
}
