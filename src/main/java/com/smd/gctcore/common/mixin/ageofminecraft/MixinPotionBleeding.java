package com.smd.gctcore.common.mixin.ageofminecraft;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to disable bleeding potion effect's damage
 * 禁用流血药水效果的伤害
 * 
 * 目标：PotionBleeding.performEffect() 方法
 * Target: PotionBleeding.performEffect() method
 * 
 * 流血效果每tick会造成伤害，此Mixin阻止该效果执行任何伤害
 * The bleeding effect causes damage per tick, this Mixin prevents it from executing any damage
 */
@Pseudo
@Mixin(targets = "net.minecraft.AgeOfMinecraft.effects.PotionBleeding", remap = false, priority = 900)
public class MixinPotionBleeding {

    /**
     * Cancel the bleeding effect's damage application
     * 取消流血效果的伤害应用
     */
    @Inject(
            method = "performEffect",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void cancelBleedingDamage(EntityLivingBase mob, int amplifier, CallbackInfo ci) {
        // 直接取消，阻止流血效果造成任何伤害
        // Cancel directly to prevent bleeding effect from causing any damage
        ci.cancel();
    }
}
