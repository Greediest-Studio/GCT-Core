package com.smd.gctcore.common.mixin.mekanism;

import com.smd.gctcore.common.integration.mekanism.DigitalMinerHarvestAccess;
import com.smd.gctcore.common.integration.mekanism.GctMekanismUpgrades;
import com.smd.gctcore.common.integration.mekanism.MekanismHarvestHelper;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import mekanism.multiblockmachine.common.tile.generator.TileEntityLargeGasGenerator;
import mekanism.multiblockmachine.common.tile.generator.TileEntityLargeWindGenerator;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Copies mining level from upgrade item NBT onto Digital Miner when installed,
 * and clears it when the mining-level upgrade is fully removed.
 * <p>
 * Also keeps {@code ENERGY_MK2} support in sync with the vanilla energy upgrade:
 * whenever a machine enables/disables {@link Upgrade#ENERGY}, the same happens for
 * {@link GctMekanismUpgrades#ENERGY_MK2}, so both share exactly the same support scope.
 */
@Mixin(value = TileComponentUpgrade.class, remap = false)
public abstract class MixinTileComponentUpgrade {

    @Shadow
    public TileEntityContainerBlock tileEntity;

    @Shadow
    public abstract void setSupported(Upgrade upgrade, boolean isSupported);

    @Inject(method = "setSupported(Lmekanism/common/Upgrade;Z)V", at = @At("RETURN"))
    private void gct$syncEnergyMk2Support(Upgrade upgrade, boolean isSupported, CallbackInfo ci) {
        if (upgrade == Upgrade.ENERGY && GctMekanismUpgrades.ENERGY_MK2 != null) {
            if (this.tileEntity instanceof TileEntityLargeGasGenerator
                    || this.tileEntity instanceof TileEntityLargeWindGenerator) {
                return;
            }
            this.setSupported(GctMekanismUpgrades.ENERGY_MK2, isSupported);
        }
    }

    @Inject(method = "onUpgradeChanged", at = @At("TAIL"))
    private void gct$recalculateOnEnergyMk2Changed(Upgrade upgrade, int previousAmount, int amount, CallbackInfo ci) {
        if (GctMekanismUpgrades.isEnergyMk2Upgrade(upgrade) && this.tileEntity != null) {
            this.tileEntity.recalculateUpgradables(Upgrade.ENERGY);
        }
    }

    @Inject(method = "onUpgradeInstalled", at = @At("TAIL"))
    private void gct$applyMiningLevelFromUpgrade(ItemStack stack, int amount, CallbackInfo ci) {
        if (amount <= 0 || stack == null || stack.isEmpty()) {
            return;
        }
        if (!(this.tileEntity instanceof TileEntityDigitalMiner)) {
            return;
        }
        if (!(this.tileEntity instanceof DigitalMinerHarvestAccess)) {
            return;
        }
        Upgrade type = Upgrade.byStack(stack);
        if (!GctMekanismUpgrades.isMiningLevelUpgrade(type)) {
            return;
        }
        int level = MekanismHarvestHelper.readMiningLevelFromStack(stack);
        ((DigitalMinerHarvestAccess) this.tileEntity).gct$setMiningLevel(level);
        Mekanism.packetHandler.sendUpdatePacket(this.tileEntity);
        this.tileEntity.markNoUpdateSync();
    }

    @Inject(method = "onUpgradeChanged", at = @At("TAIL"))
    private void gct$clearMiningLevelOnRemove(Upgrade upgrade, int previousAmount, int amount, CallbackInfo ci) {
        if (!(this.tileEntity instanceof TileEntityDigitalMiner)) {
            return;
        }
        if (!(this.tileEntity instanceof DigitalMinerHarvestAccess)) {
            return;
        }
        if (!GctMekanismUpgrades.isMiningLevelUpgrade(upgrade)) {
            return;
        }
        if (amount <= 0) {
            ((DigitalMinerHarvestAccess) this.tileEntity).gct$setMiningLevel(0);
            Mekanism.packetHandler.sendUpdatePacket(this.tileEntity);
            this.tileEntity.markNoUpdateSync();
        }
    }
}
