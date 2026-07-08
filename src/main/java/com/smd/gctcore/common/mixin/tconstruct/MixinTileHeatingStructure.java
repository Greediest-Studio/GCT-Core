package com.smd.gctcore.common.mixin.tconstruct;

import com.smd.gctcore.common.config.GCTCoreConfig;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.tconstruct.smeltery.tileentity.TileHeatingStructure;
import sol3675.smeltryaccelerator.BlockSmelteryAccelerator;

@Mixin(value = TileHeatingStructure.class, remap = false)
public class MixinTileHeatingStructure {

    @Inject(method = "heatSlot", at = @At("RETURN"), cancellable = true)
    private void gctcore_accelerateHeatSlot(int slot, CallbackInfoReturnable<Integer> cir) {
        TileEntity te = (TileEntity) (Object) this;
        if (te.getWorld() == null || te.getWorld().isRemote) return;
        if (!(te.getWorld().getBlockState(te.getPos().up()).getBlock() instanceof BlockSmelteryAccelerator)) return;

        int multiplier = GCTCoreConfig.smelteryIntegration.smelteryMultiplier;
        if (multiplier > 1) {
            cir.setReturnValue(cir.getReturnValueI() * multiplier);
        }
    }
}
