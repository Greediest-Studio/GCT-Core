package com.smd.gctcore.common.mixin.compactmachines3;

import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

/**
 * Null-safety guard for {@code AnnotatedInstanceUtil} once {@code asmData} is cleared after {@code postInit}.
 */
@Pseudo
@Mixin(targets = "org.dave.compactmachines3.utility.AnnotatedInstanceUtil", remap = false)
public abstract class MixinAnnotatedInstanceUtil {

    @Shadow(remap = false)
    public static ASMDataTable asmData;

    @Inject(method = "getNullHandlers()Ljava/util/List;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gctcore$guardNullHandlers(CallbackInfoReturnable<List<?>> cir) {
        if (asmData == null) {
            cir.setReturnValue(Collections.emptyList());
        }
    }

    @Inject(method = "getExtraTileDataProviders()Ljava/util/List;", at = @At("HEAD"), cancellable = true, remap = false)
    private static void gctcore$guardExtraTileDataProviders(CallbackInfoReturnable<List<?>> cir) {
        if (asmData == null) {
            cir.setReturnValue(Collections.emptyList());
        }
    }
}
