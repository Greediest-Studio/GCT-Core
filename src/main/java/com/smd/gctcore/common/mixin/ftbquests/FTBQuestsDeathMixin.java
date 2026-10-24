package com.smd.gctcore.common.mixin.ftbquests;

import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.feed_the_beast.ftbquests.FTBQuestsEventHandler", remap = false)
public class FTBQuestsDeathMixin {
    private static final ResourceLocation STORM = new ResourceLocation("witherstormmod:wither_storm");

    @Inject(method = "onPlayerKill", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gctcore$settledVictory(LivingDeathEvent event, CallbackInfo callback) {
        // 风暴的击杀任务统一由核心终战事件结算。
        if (STORM.equals(EntityList.getKey(event.getEntityLiving()))) callback.cancel();
    }
}
