package com.smd.gctcore.common.mixin.ssa;

import com.smd.gctcore.common.config.GctCoreConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sol3675.smeltryaccelerator.BlockSmelteryAccelerator;

import java.util.Random;

@Mixin(BlockSmelteryAccelerator.class)
public class MixinBlockSmelteryAccelerator {

    @Inject(method = "updateTick", at = @At("HEAD"), cancellable = true)
    private void gctcore_UpdateTick(World worldIn, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        ci.cancel();
    }
}
