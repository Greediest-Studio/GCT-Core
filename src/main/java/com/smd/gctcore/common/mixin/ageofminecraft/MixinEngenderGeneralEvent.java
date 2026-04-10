package com.smd.gctcore.common.mixin.ageofminecraft;

import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to completely disable EnderDragon death event that spawns summoner dragon and Darkness
 * 完全禁用末影龙死亡后生成召唤师版末影龙和Darkness的事件
 * 
 * 目标代码位置：EngenderGeneralEvent.onMobDeathEvent() 方法中第530-556行
 * Target code location: Lines 530-556 in EngenderGeneralEvent.onMobDeathEvent()
 * 
 * 当末影龙死亡时，Engender Mod会：
 * 1. 检查附近256格范围内是否有召唤师版末影龙(EntityEnderDragon)
 * 2. 如果没有找到且有10%概率，生成一个Darkness实体
 * 
 * 此Mixin将完全阻止这个逻辑执行
 */
@Pseudo
@Mixin(targets = "net.minecraft.AgeOfMinecraft.events.EngenderGeneralEvent", remap = false, priority = 900)
public class MixinEngenderGeneralEvent {

    /**
     * Prevent spawning of summoner dragon and Darkness when vanilla EnderDragon dies
     * 阻止原版末影龙死亡时生成召唤师末影龙和Darkness
     * 
     * 在方法开头检查并提前返回，避免整个死亡处理逻辑
     */
    @Inject(
            method = "onMobDeathEvent",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0  // 设置为可选，避免在没有Engender Mod时崩溃
    )
    private void preventEnderDragonDeathSpawns(LivingDeathEvent event, CallbackInfo ci) {
        // 检查死亡实体是否为原版末影龙
        // Check if the dying entity is vanilla EnderDragon
        // 好像没必要多写个方法，修玩家死亡概率崩溃的先塞这里了（
        if (event.getEntity() instanceof EntityDragon || event.getEntity() instanceof EntityPlayer) {
            // 提前返回，完全跳过末影龙&玩家死亡后的生成逻辑
            // Return early, completely skip the spawn logic after EnderDragon death
            ci.cancel();
        }
    }

    @Inject(
            method = "lootLoad",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0  // 设置为可选，避免在没有Engender Mod时崩溃
    )
    public void stoplootLoad(LootTableLoadEvent event, CallbackInfo ci) {
        //不许往战利品表塞垃圾喵
        ci.cancel();
    }
}
