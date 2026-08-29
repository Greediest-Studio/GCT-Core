package com.smd.gctcore.common.mixin.babymobs;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents Baby Mobs' sensitive wither pattern from consuming normal wither builds. */
@Mixin(targets = "furgl.babyMobs.common.event.BlockPatternEvent", remap = false)
@Pseudo
public abstract class MixinBlockPatternEvent {
    @Inject(method = "onEvent", at = @At("HEAD"), cancellable = true)
    private void gctcore$requireIsolatedSoulSand(BlockEvent.PlaceEvent event, CallbackInfo ci) {
        World world = event.getWorld();
        BlockPos skull = event.getPos();
        if (world.isRemote || world.getBlockState(skull).getBlock() != Blocks.SKULL) {
            return;
        }
        BlockPos soulSand = skull.down();
        if (world.getBlockState(soulSand).getBlock() != Blocks.SOUL_SAND) {
            return;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos adjacent = soulSand.offset(facing);
            if (world.getBlockState(adjacent).getBlock() == Blocks.SOUL_SAND
                    || world.getBlockState(adjacent).getBlock() == Blocks.COMMAND_BLOCK
                    || world.getBlockState(adjacent).getBlock() == Blocks.REPEATING_COMMAND_BLOCK
                    || world.getBlockState(adjacent).getBlock() == Blocks.CHAIN_COMMAND_BLOCK) {
                ci.cancel();
                return;
            }
        }
    }
}
