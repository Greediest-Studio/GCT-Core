package com.smd.gctcore.common.mixin.vanilla.damageparticle;

import net.minecraft.entity.Entity;
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
    private void gctcore$disableTeamCheckEntity(Entity entityIn, CallbackInfoReturnable<Boolean> cir) {
        String className = this.getClass().getName();
        if (className.contains("EntityFriendlyCreature")) {
            cir.setReturnValue(false);
        }
    }
}
