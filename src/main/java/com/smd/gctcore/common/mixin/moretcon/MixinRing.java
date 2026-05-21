package com.smd.gctcore.common.mixin.moretcon;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.existingeevee.moretcon.item.tooltypes.Ring;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Ring.class)
public abstract class MixinRing {

    /**
     * @author Gct-Core
     * @reason 非法运算
     */
    @Overwrite(remap = false)
    private boolean shouldTick(EntityLivingBase player) {
        if (player instanceof EntityPlayer) {
            IBaublesItemHandler handler = BaublesApi.getBaublesHandler((EntityPlayer) player);
            int count = 0;
            for (int i = 0; i < handler.getSlots(); i++) {
                if (handler.getStackInSlot(i).getItem() instanceof Ring) {
                    count++;
                }
            }
            if (count == 0) {
                return false;
            }
            return player.getEntityWorld().getWorldTime() % ((long) count * count) == 0;
        }
        return false;
    }
}
