package com.smd.gctcore.common.mixin.gctmobs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.mcreator.gctmobs.procedure.ProcedureProKabalahBuilderRecipe", remap = false)
public abstract class MixinProcedureProKabalahBuilderRecipe {

    @Inject(method = "executeProcedure", at = @At("HEAD"), cancellable = true)
    private static void gctcore$cancelKabalahProcedure(java.util.Map<String, Object> dependencies, CallbackInfo ci) {
        // Cancel the entire procedure: remove all recipe checks and the "clear output slot when no recipe" behavior.
        // This intentionally does nothing else so other unrelated events (like taking from output slot) remain unaffected.
        ci.cancel();
    }
}
