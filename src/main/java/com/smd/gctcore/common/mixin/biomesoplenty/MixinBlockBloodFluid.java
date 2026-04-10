package com.smd.gctcore.common.mixin.biomesoplenty;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "biomesoplenty.common.fluids.blocks.BlockBloodFluid", remap = false)
public class MixinBlockBloodFluid {

    private static final ThreadLocal<Boolean> IN_CHECK = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(method = "checkForMixing", at = @At("HEAD"), cancellable = true)
    private void gctcore$preventRecursiveMixing(World worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (IN_CHECK.get()) {
            cir.setReturnValue(false);
            return;
        }

        IN_CHECK.set(Boolean.TRUE);
        // We don't clear here; we'll clear in a finally-like injection at RETURN
    }

    @Inject(method = "checkForMixing", at = @At("RETURN"))
    private void gctcore$clearGuard(World worldIn, BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        IN_CHECK.remove();
    }
}
