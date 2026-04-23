package com.smd.gctcore.common.mixin.vanilla.entity;

import net.minecraft.entity.boss.EntityWither;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityWither.class)
public abstract class MixinEntityWither {

    @Unique
    private static final ResourceLocation WITHER_LOOT =
            LootTableList.register(new ResourceLocation("minecraft", "entities/boss/wither"));

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        ((EntityWither) (Object) this).deathLootTable = WITHER_LOOT;
    }
}