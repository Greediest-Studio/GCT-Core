package com.smd.gctcore.common.mixin.quantumthings;

import com.smd.gctcore.common.integration.mekanism.TimeAcceleratedUpdateAccess;
import lumien.randomthings.entitys.EntityTimeAccelerator;
import net.minecraft.util.ITickable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Bridges Quantum Things' Time in a Bottle calls to Mekanism's controlled extra-tick entry point.
 */
@Mixin(value = EntityTimeAccelerator.class, remap = true)
public abstract class MixinEntityTimeAccelerator {

    @Redirect(
            method = "onEntityUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/ITickable;update()V",
                    remap = true
            )
    )
    private void gct$accelerateMekanism(ITickable target) {
        if (target instanceof TimeAcceleratedUpdateAccess) {
            ((TimeAcceleratedUpdateAccess) target).gct$timeAcceleratedUpdate();
        } else {
            target.update();
        }
    }
}
