package com.smd.gctcore.common.mixin.pewter;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(value = com.ejektaflex.pewter.logic.MaterialRegistrar.class, remap = false)
public class MixinMaterialRegistrar {

    @Inject(method = "makeFluid", at = @At("HEAD"), cancellable = true, remap = false)
    private void onMakeFluid(CallbackInfo ci) {
        try {
            // Reflectively access the 'data' field
            Field dataField = this.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            Object data = dataField.get(this);
            if (data == null) return;

            // call getCraftable()
            Method getCraftable = data.getClass().getMethod("getCraftable");
            Object craftableObj = getCraftable.invoke(data);
            if (craftableObj instanceof Boolean && ((Boolean) craftableObj)) {
                ci.cancel();
                return;
            }

            // call getName()
            Method getName = data.getClass().getMethod("getName");
            Object nameObj = getName.invoke(data);
            if (nameObj == null) return;
            String materialName = nameObj.toString();
            String fluidName = materialName.toLowerCase();

            Fluid existing = FluidRegistry.getFluid(fluidName);
            if (existing != null) {
                Field fluidField = this.getClass().getDeclaredField("fluid");
                fluidField.setAccessible(true);
                fluidField.set(this, existing);
                ci.cancel();
                return;
            }
        } catch (Throwable t) {
            // If anything goes wrong, allow original logic to proceed
        }
    }
}
