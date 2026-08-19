package com.smd.gctcore.common.mixin.mmce;

import com.smd.gctcore.common.tile.blood_altar.BloodAltarMachine;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.RecipePrimer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps the original script's display-name thread values compatible with the
 * stable core-thread identifiers. MMCE does not localize names while searching
 * recipes: it requires an exact raw-string match.
 */
@Mixin(value = RecipePrimer.class, remap = false)
public abstract class MixinRecipePrimerThreadName {

    @Shadow
    @Final
    protected ResourceLocation machineName;

    @Shadow
    private String threadName;

    @Inject(method = "setThreadName", at = @At("RETURN"))
    private void gctcore$normalizeBloodAltarThreadName(final String name,
                                                        final CallbackInfoReturnable<RecipePrimer> callback) {
        if (BloodAltarMachine.MACHINE_ID.equals(machineName)) {
            threadName = BloodAltarMachine.canonicalThreadName(threadName);
        }
    }
}
