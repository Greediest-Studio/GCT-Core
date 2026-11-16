package com.smd.gctcore.common.mixin.aether;

import com.gildedgames.the_aether.entities.hostile.EntityZephyr;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.translation.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinAetherZephyrName {

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void gctcore$useUniqueZephyrTranslation(CallbackInfoReturnable<String> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof EntityZephyr && !entity.hasCustomName()) {
            cir.setReturnValue(I18n.translateToLocal("entity.gctcore.aether_zephyr.name"));
        }
    }
}
