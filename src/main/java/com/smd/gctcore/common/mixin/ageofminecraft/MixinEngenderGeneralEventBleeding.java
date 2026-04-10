package com.smd.gctcore.common.mixin.ageofminecraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to prevent bleeding effect from being applied on damage
 * 阻止受伤时应用流血效果
 * 
 * 目标：EngenderGeneralEvent.onMobHitEvent() 方法
 * Target: EngenderGeneralEvent.onMobHitEvent() method
 * 
 * 当实体受到伤害时，Engender Mod会在第365-392行应用流血效果
 * 此Mixin在方法开头检测并取消，阻止流血效果被应用
 * 
 * When an entity takes damage, Engender Mod applies bleeding effect in lines 365-392
 * This Mixin checks at method start and cancels to prevent bleeding effect from being applied
 */
@Pseudo
@Mixin(targets = "net.minecraft.AgeOfMinecraft.events.EngenderGeneralEvent", remap = false, priority = 900)
public class MixinEngenderGeneralEventBleeding {

    /**
     * Prevent bleeding effect from being applied when entity takes damage
     * 阻止实体受伤时应用流血效果
     * 
     * 在方法开头就取消整个事件处理，阻止流血效果应用
     * Cancel entire event handling at method start to prevent bleeding effect application
     */
    @Inject(
            method = "onMobHitEvent",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void preventBleedingInHurtEvent(LivingHurtEvent event, CallbackInfo ci) {
        // 直接取消整个事件处理，这样就不会应用流血效果
        // 但这会影响该事件的所有其他逻辑，需要更精确的注入点
        // Directly cancel entire event handling to prevent bleeding effect
        // But this affects all other logic in this event, need more precise injection point
        
        // 暂时注释掉，使用PotionBleeding的Mixin更安全
        // Temporarily commented out, using PotionBleeding Mixin is safer
        // ci.cancel();
    }
}
