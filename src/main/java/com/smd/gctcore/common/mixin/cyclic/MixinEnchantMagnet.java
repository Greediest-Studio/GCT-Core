package com.smd.gctcore.common.mixin.cyclic;

import com.lothrazar.cyclicmagic.enchant.EnchantMagnet;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EnchantMagnet.class)
public class MixinEnchantMagnet {

    @Inject(method = "onEntityUpdate", at = @At("HEAD"), cancellable = true, remap = false)
    private void disableMagnet(LivingUpdateEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}
