package com.smd.gctcore.common.mixin.ageofminecraft;

import net.minecraft.AgeOfMinecraft.EngenderEventHandler;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EngenderEventHandler.class)
public class MixinEngenderEventHandler {

    @Inject(method = "onMobDeathEvent", at = @At("HEAD"), cancellable = true, remap = false)
    private void cancelManaOrbDrop(LivingDeathEvent event, CallbackInfo ci) {
        ci.cancel();
    }
}

