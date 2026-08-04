package com.smd.gctcore.common.mixin.mekanism;

import com.smd.gctcore.common.integration.mekanism.GctMekanismUpgrades;
import mekanism.common.base.IUpgradeTile;
import mekanism.common.util.MekanismUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MekanismUtils.class, remap = false)
public abstract class MixinMekanismUtils {

    @Inject(method = "getMaxEnergy(Lmekanism/common/base/IUpgradeTile;D)D",
            at = @At("RETURN"), cancellable = true)
    private static void gct$applyEnergyMk2Multiplier(IUpgradeTile mgmt, double def,
                                                     CallbackInfoReturnable<Double> cir) {
        if (GctMekanismUpgrades.ENERGY_MK2 == null || mgmt == null) {
            return;
        }
        int level = mgmt.getInstalledUpgrades(GctMekanismUpgrades.ENERGY_MK2);
        if (level > 0) {
            cir.setReturnValue(cir.getReturnValueD() * Math.pow(2, level));
        }
    }
}
