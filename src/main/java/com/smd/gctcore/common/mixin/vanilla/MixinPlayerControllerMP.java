package com.smd.gctcore.common.mixin.vanilla;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.multiplayer.WorldClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    /**
     * Some client integrations still return FAIL before calling ItemBlock#onItemUse when the
     * player cannot use command blocks. Re-run the normal placement path after that early return;
     * returning SUCCESS lets Minecraft's right-click handler play the hand swing animation.
     */
    @Inject(method = "processRightClickBlock", at = @At("RETURN"), cancellable = true)
    private void gctcore$restoreRestrictedPlacementAnimation(
            EntityPlayerSP player, WorldClient world, BlockPos pos, EnumFacing facing, Vec3d hit,
            EnumHand hand, CallbackInfoReturnable<EnumActionResult> cir) {
        if (cir.getReturnValue() == EnumActionResult.SUCCESS) {
            return;
        }

        ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof ItemBlock) || !isRestrictedBlock(((ItemBlock) stack.getItem()).getBlock())
                || !((ItemBlock) stack.getItem()).canPlaceBlockOnSide(world, pos, facing, player, stack)) {
            return;
        }

        EnumActionResult placement = stack.onItemUse(player, world, pos, hand, facing,
                (float) (hit.x - pos.getX()), (float) (hit.y - pos.getY()), (float) (hit.z - pos.getZ()));
        if (placement == EnumActionResult.SUCCESS) {
            cir.setReturnValue(EnumActionResult.SUCCESS);
        }
    }

    private static boolean isRestrictedBlock(Block block) {
        return block instanceof BlockCommandBlock || block instanceof BlockStructure;
    }
}
