package com.smd.gctcore.common.mixin.mekanism;

import com.smd.gctcore.common.integration.mekanism.GctMekanismUpgrades;
import mekanism.common.Upgrade;
import mekanism.common.base.IUpgradeTile;
import mekceumoremachine.common.tile.machine.TileEntityTierAmbientAccumulator;
import mekceumoremachine.common.tile.machine.TileEntityTierChemicalInfuser;
import mekceumoremachine.common.tile.machine.TileEntityTierChemicalWasher;
import mekceumoremachine.common.tile.machine.TileEntityTierElectrolyticSeparator;
import mekceumoremachine.common.tile.machine.TileEntityTierIsotopicCentrifuge;
import mekceumoremachine.common.tile.machine.TileEntityTierRotaryCondensentrator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * MoreMachine's machines replace Mekanism's upgrade information lookup with
 * {@code getMultScaledInfo()}, whose default multiplier is capped at 10.
 */
@Mixin(value = {
        TileEntityTierAmbientAccumulator.class,
        TileEntityTierChemicalInfuser.class,
        TileEntityTierChemicalWasher.class,
        TileEntityTierElectrolyticSeparator.class,
        TileEntityTierIsotopicCentrifuge.class,
        TileEntityTierRotaryCondensentrator.class
}, remap = false)
public abstract class MixinMoreMachineUpgradeInfo {

    @Inject(method = "getInfo(Lmekanism/common/Upgrade;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void gct$showEnergyMk2Multiplier(Upgrade upgrade, CallbackInfoReturnable<List<String>> cir) {
        if (!GctMekanismUpgrades.isEnergyMk2Upgrade(upgrade)) {
            return;
        }
        IUpgradeTile tile = (IUpgradeTile) (Object) this;
        cir.setReturnValue(GctMekanismUpgrades.energyMk2Info(upgrade, tile));
    }
}
