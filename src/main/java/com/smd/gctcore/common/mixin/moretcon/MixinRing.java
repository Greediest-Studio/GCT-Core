package com.smd.gctcore.common.mixin.moretcon;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.existingeevee.moretcon.item.tooltypes.Ring;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ring.class)
public abstract class MixinRing {

    @Inject(
            method = "shouldTick",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void gctCore$fixShouldTick(EntityLivingBase player, CallbackInfoReturnable<Boolean> cir) {
        if (player instanceof EntityPlayer) {
            IBaublesItemHandler handler = BaublesApi.getBaublesHandler((EntityPlayer) player);
            int count = 0;
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.getStackInSlot(i).getItem() instanceof Ring) {
                    count++;
                }
            }
            if (count == 0) {
                cir.setReturnValue(false);
                return;
            }
            cir.setReturnValue(player.getEntityWorld().getWorldTime() % ((long) count * count) == 0);
            return;
        }
        cir.setReturnValue(false);
    }
}
