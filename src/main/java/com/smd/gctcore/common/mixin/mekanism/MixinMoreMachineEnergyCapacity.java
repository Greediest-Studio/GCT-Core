package com.smd.gctcore.common.mixin.mekanism;

import com.smd.gctcore.common.integration.mekanism.GctMekanismUpgrades;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekceumoremachine.common.tile.machine.TileEntityTierAmbientAccumulator;
import mekceumoremachine.common.tile.machine.TileEntityTierChemicalInfuser;
import mekceumoremachine.common.tile.machine.TileEntityTierChemicalWasher;
import mekceumoremachine.common.tile.machine.TileEntityTierElectricPump;
import mekceumoremachine.common.tile.machine.TileEntityTierElectrolyticSeparator;
import mekceumoremachine.common.tile.machine.TileEntityTierIsotopicCentrifuge;
import mekceumoremachine.common.tile.machine.TileEntityTierRotaryCondensentrator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies the custom energy upgrade to MoreMachine classes that override
 * {@code getMaxEnergy()} and only check the vanilla energy upgrade.
 */
@Mixin(value = {
        TileEntityTierAmbientAccumulator.class,
        TileEntityTierChemicalInfuser.class,
        TileEntityTierChemicalWasher.class,
        TileEntityTierElectricPump.class,
        TileEntityTierElectrolyticSeparator.class,
        TileEntityTierIsotopicCentrifuge.class,
        TileEntityTierRotaryCondensentrator.class
}, remap = false)
public abstract class MixinMoreMachineEnergyCapacity {

    @Inject(method = "getMaxEnergy()D", at = @At("RETURN"), cancellable = true)
    private void gct$applyEnergyMk2Multiplier(CallbackInfoReturnable<Double> cir) {
        if (GctMekanismUpgrades.ENERGY_MK2 == null) {
            return;
        }
        IUpgradeTile tile = (IUpgradeTile) (Object) this;
        int level = tile.getInstalledUpgrades(GctMekanismUpgrades.ENERGY_MK2);
        if (level <= 0 || tile.getInstalledUpgrades(Upgrade.ENERGY) > 0) {
            return;
        }
        cir.setReturnValue(cir.getReturnValueD() * Math.pow(2, level));
    }
}
