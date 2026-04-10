package com.smd.gctcore.common.mixin.damageparticle;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Inject(
        method = "isOnSameTeam(Lnet/minecraft/entity/Entity;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void gctcore$disableTeamCheckEntity(Entity other, CallbackInfoReturnable<Boolean> cir) {
        // 检查类名而不是直接 instanceof，避免 ClassNotFoundException
        // Check class name instead of direct instanceof to avoid ClassNotFoundException
        String className = this.getClass().getName();
        if (className.contains("EntityFriendlyCreature")) {
            cir.setReturnValue(false);
        }
    }
}
