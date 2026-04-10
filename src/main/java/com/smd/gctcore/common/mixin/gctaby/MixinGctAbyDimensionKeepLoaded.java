package com.smd.gctcore.common.mixin.gctaby;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(
    targets = {
        "net.mcreator.gct_aby.world.WorldDIMDarkerRealm",
        "net.mcreator.gct_aby.world.WorldWarpedRuin"
    },
    remap = false
)
public abstract class MixinGctAbyDimensionKeepLoaded {

    @ModifyArg(
        method = "preInit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/DimensionType;register(Ljava/lang/String;Ljava/lang/String;ILjava/lang/Class;Z)Lnet/minecraft/world/DimensionType;"
        ),
        index = 4
    )
    private boolean gctcore$disableAlwaysLoaded(boolean keepLoaded) {
        return false;
    }
}
