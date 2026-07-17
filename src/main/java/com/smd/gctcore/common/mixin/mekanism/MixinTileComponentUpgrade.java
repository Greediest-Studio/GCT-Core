package com.smd.gctcore.common.mixin.mekanism;

import com.smd.gctcore.common.integration.mekanism.DigitalMinerHarvestAccess;
import com.smd.gctcore.common.integration.mekanism.GctMekanismUpgrades;
import com.smd.gctcore.common.integration.mekanism.MekanismHarvestHelper;
import mekanism.common.Mekanism;
import mekanism.common.Upgrade;
import mekanism.common.tile.component.TileComponentUpgrade;
import mekanism.common.tile.machine.TileEntityDigitalMiner;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Copies mining level from upgrade item NBT onto Digital Miner when installed,
 * and clears it when the mining-level upgrade is fully removed.
 */
@Mixin(value = TileComponentUpgrade.class, remap = false)
public abstract class MixinTileComponentUpgrade {

    @Shadow
    public TileEntityContainerBlock tileEntity;

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
        // Full TE packet so clients watching the main GUI update Level immediately
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
