package com.smd.gctcore.common.mixin.vanilla;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Keeps the client-side result in sync with the server-side survival placement change.
 * A successful result is required for Minecraft to trigger the hand swing animation.
 */
@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

    @Redirect(
            method = "processRightClickBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;canUseCommandBlock()Z"
            )
    )
    private boolean gctcore$allowRestrictedBlockPlacement(EntityPlayer player) {
        return true;
    }
}
