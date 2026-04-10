package com.smd.gctcore.common.mixin.gctmobs;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.mcreator.gctmobs.world.WorldAlfheim$BlockCustomPortal", remap = false)
public abstract class MixinAlfheimPortal {

    @Inject(method = "portalSpawn", at = @At("HEAD"), cancellable = true)
    private void gctcore$preventPortalSpawn(World world, BlockPos pos, CallbackInfo ci) {
        // Stop the Alfheim portal from forming new frames
        ci.cancel();
    }

    @Inject(method = "func_180634_a", at = @At("HEAD"), cancellable = true)
    private void gctcore$preventPortalUse(World world, BlockPos pos, IBlockState state, Entity entity, CallbackInfo ci) {
        // Prevent all entity collisions from triggering dimension transfer
        ci.cancel();
    }
}
