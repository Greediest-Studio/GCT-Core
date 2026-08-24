package com.smd.gctcore.common.mixin.betterendforge;

import com.smd.gctcore.common.integration.betterendforge.BetterEndPillarHeightCompat;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Persists the pillar cache returned by BetterEndForge's WorldDataAPI. The
 * target mod's implementation only keeps this data in a static map.
 */
@Pseudo
@Mixin(targets = "mod.beethoven92.betterendforge.common.util.WorldDataAPI", remap = false)
public abstract class MixinWorldDataAPI {

    @Inject(
            method = "getCompoundTag(Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/nbt/NBTTagCompound;",
            at = @At("RETURN"),
            remap = false
    )
    private static void gctcore$persistPillarHeights(String modID,
                                                     String path,
                                                     CallbackInfoReturnable<NBTTagCompound> cir) {
        if ("betterendforge".equals(modID) && "pillars".equals(path)) {
            BetterEndPillarHeightCompat.synchronize(cir.getReturnValue());
        }
    }
}
