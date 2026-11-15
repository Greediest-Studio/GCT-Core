package com.smd.gctcore.common.mixin.astralsorcery.perktree;

import hellfirepvp.astralsorcery.client.util.BufferBatch;
import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BufferBatch.class, remap = false)
public interface AccessorBufferBatch {

    @Accessor("buffer")
    BufferBuilder gctcore$getBuffer();
}
