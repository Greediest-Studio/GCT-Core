package com.smd.gctcore.common.mixin.compactmachines3;

import com.smd.gctcore.gctcore;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

/**
 * CompactMachines3 keeps the entire FML {@code ASMDataTable} in {@code AnnotatedInstanceUtil.asmData}
 * after startup even though both annotation lookups finish during {@code postInit}.
 * Clearing the static reference lets the GC reclaim the retained ASM discovery tables.
 */
@Pseudo
@Mixin(targets = "org.dave.compactmachines3.CompactMachines3", remap = false)
public abstract class MixinCompactMachines3 {

    @Inject(
            method = "postInit(Lnet/minecraftforge/fml/common/event/FMLPostInitializationEvent;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void gctcore$clearAsmDataAfterPostInit(FMLPostInitializationEvent event, CallbackInfo ci) {
        try {
            Class<?> utilClass = Class.forName("org.dave.compactmachines3.utility.AnnotatedInstanceUtil");
            Field asmDataField = utilClass.getField("asmData");
            if (asmDataField.get(null) != null) {
                asmDataField.set(null, null);
                gctcore.LOGGER.info("GCT-Core cleared CompactMachines3 AnnotatedInstanceUtil.asmData after postInit");
            }
        } catch (Throwable t) {
            gctcore.LOGGER.warn("Failed to clear CompactMachines3 AnnotatedInstanceUtil.asmData", t);
        }
    }
}
