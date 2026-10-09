package com.smd.gctcore.common.mixin.projecte;

import com.smd.gctcore.common.integration.mekanism.TimeAcceleratedUpdateAccess;
import net.minecraft.util.ITickable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Bridges ProjectE's Watch of Flowing Time calls to Mekanism's controlled extra-tick entry point.
 */
@Pseudo
@Mixin(targets = "moze_intel.projecte.gameObjs.items.TimeWatch", remap = false)
public abstract class MixinTimeWatch {

    @Redirect(
            method = "speedUpTileEntities",
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
