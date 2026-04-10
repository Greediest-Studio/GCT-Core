package com.smd.gctcore.common.mixin.gctores;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(
    targets = {
        "net.mcreator.gctores.world.WorldDIM100",
        "net.mcreator.gctores.world.WorldDIM101",
        "net.mcreator.gctores.world.WorldDIM102"
    },
    remap = false
)
public abstract class MixinGctOresDimensionKeepLoaded {

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
