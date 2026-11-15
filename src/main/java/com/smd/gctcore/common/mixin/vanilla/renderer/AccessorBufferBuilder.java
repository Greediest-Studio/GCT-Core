package com.smd.gctcore.common.mixin.vanilla.renderer;

import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public interface AccessorBufferBuilder {

    @Accessor("byteBuffer")
    ByteBuffer gctcore$getByteBuffer();
}
