package com.smd.gctcore.common.mixin.betterendforge;

import net.minecraft.world.gen.feature.WorldGenSpikes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldGenSpikes.EndSpike.class)
public interface AccessorEndSpike {

    @Accessor("height")
    int gctcore$getRawHeight();
}
