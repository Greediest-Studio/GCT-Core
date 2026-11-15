package com.smd.gctcore.common.mixin.astralsorcery.perktree;

import hellfirepvp.astralsorcery.client.gui.perk.BatchPerkContext;
import hellfirepvp.astralsorcery.client.util.BufferBatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.TreeMap;

@Mixin(value = BatchPerkContext.class, remap = false)
public interface AccessorBatchPerkContext {

    @Accessor("bufferGroups")
    TreeMap<BatchPerkContext.TextureObjectGroup, BufferBatch> gctcore$getBufferGroups();
}
