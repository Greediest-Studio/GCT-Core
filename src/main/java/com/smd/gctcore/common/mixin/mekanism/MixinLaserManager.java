package com.smd.gctcore.common.mixin.mekanism;

import com.smd.gctcore.common.config.GCTCompatConfig;
import com.smd.gctcore.common.integration.mekanism.MekanismHarvestHelper;
import mekanism.api.Coord4D;
import mekanism.common.LaserManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Prevents lasers from breaking blocks above the configured harvest level.
 */
@Mixin(value = LaserManager.class, remap = false)
public class MixinLaserManager {

    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    private static void gct$limitLaserBreak(Coord4D blockCoord, boolean dropAtBlock, World world, BlockPos laserPos,
                                            CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!GCTCompatConfig.mekanismIntegration.enableLaserHarvestLimit) {
            return;
        }
        try {
            IBlockState state = blockCoord.getBlockState(world);
            if (!MekanismHarvestHelper.canLaserMine(state)) {
                cir.setReturnValue(null);
            }
        } catch (Throwable ignored) {
            // Be permissive if a modded block throws during harvest checks.
        }
    }
}
