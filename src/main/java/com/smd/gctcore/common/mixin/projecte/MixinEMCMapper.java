package com.smd.gctcore.common.mixin.projecte;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Pseudo
@Mixin(targets = "moze_intel.projecte.emc.EMCMapper", remap = false)
public abstract class MixinEMCMapper {
    @ModifyArgs(method = "map()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/common/config/Configuration;getBoolean(Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;)Z"), require = 1)
    private static void gctcore$defaultLogEMCExploitsOff(Args args) {
        if ("logEMCExploits".equals(args.get(0))) {
            args.set(2, false);
        }
    }
}
