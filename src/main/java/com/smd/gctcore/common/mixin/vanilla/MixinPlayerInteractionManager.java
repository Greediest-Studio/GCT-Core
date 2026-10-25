package com.smd.gctcore.common.mixin.vanilla;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows command blocks and structure blocks to be placed by survival players.
 *
 * <p>Vanilla performs this permission check in
 * {@link PlayerInteractionManager#processRightClickBlock(EntityPlayer, net.minecraft.world.World,
 * net.minecraft.item.ItemStack, net.minecraft.util.EnumHand, net.minecraft.util.math.BlockPos,
 * net.minecraft.util.EnumFacing, float, float, float)} before calling {@code ItemBlock#onItemUse}.
 * Redirecting this invocation keeps the normal command-block permission checks for editing and
 * breaking already placed blocks unchanged.</p>
 */
@Mixin(PlayerInteractionManager.class)
public abstract class MixinPlayerInteractionManager {

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
