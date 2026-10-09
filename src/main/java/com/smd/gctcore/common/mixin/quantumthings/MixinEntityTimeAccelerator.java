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
            gct$vanillaUpdate(target);
        }
    }

    private static void gct$vanillaUpdate(ITickable target) {
        try {
            for (java.lang.reflect.Method method : ITickable.class.getMethods()) {
                if (method.getParameterTypes().length == 0) {
                    method.invoke(target);
                    return;
                }
            }
            throw new NoSuchMethodException("ITickable tick method");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to invoke tickable update", e);
        }
    }
}
