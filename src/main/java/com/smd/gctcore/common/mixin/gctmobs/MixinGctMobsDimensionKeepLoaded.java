package com.smd.gctcore.common.mixin.gctmobs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(
    targets = {
        "net.mcreator.gctmobs.world.WorldAlfheim",
        "net.mcreator.gctmobs.world.WorldAtlantis",
        "net.mcreator.gctmobs.world.WorldEverheaven",
        "net.mcreator.gctmobs.world.WorldStarland",
        "net.mcreator.gctmobs.world.WorldBesideVoid"
    },
    remap = false
)
public abstract class MixinGctMobsDimensionKeepLoaded {

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
