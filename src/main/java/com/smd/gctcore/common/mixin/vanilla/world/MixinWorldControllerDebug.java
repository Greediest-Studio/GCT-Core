package com.smd.gctcore.common.mixin.vanilla.world;

import com.smd.gctcore.common.integration.mmce.MMCE_ControllerDebug;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorldControllerDebug {

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z", at = @At("HEAD"))
    private void gctcore$captureControllerOldState(BlockPos pos, IBlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        World world = (World) (Object) this;
        MMCE_ControllerDebug.traceControllerBlockStateChange(world, pos, world.getBlockState(pos), newState, flags);
    }
}
