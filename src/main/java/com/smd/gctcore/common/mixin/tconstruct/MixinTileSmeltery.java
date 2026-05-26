package com.smd.gctcore.common.mixin.tconstruct;

import com.smd.gctcore.common.config.GctCoreConfig;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import slimeknights.tconstruct.smeltery.tileentity.TileSmeltery;
import sol3675.smeltryaccelerator.BlockSmelteryAccelerator;

@Mixin(value = TileSmeltery.class, remap = false)
public class MixinTileSmeltery {

    @ModifyConstant(method = "alloyAlloys", constant = @Constant(intValue = 10))
    private int gctcore_accelerateAlloyConstant(int constant) {
        TileEntity te = (TileEntity) (Object) this;
        if (te.getWorld() == null || te.getWorld().isRemote) return constant;
        if (!(te.getWorld().getBlockState(te.getPos().up()).getBlock() instanceof BlockSmelteryAccelerator)) return constant;

        int multiplier = GctCoreConfig.smelteryIntegration.smelteryMultiplier;
        return multiplier > 1 ? constant * multiplier : constant;

    }
}
