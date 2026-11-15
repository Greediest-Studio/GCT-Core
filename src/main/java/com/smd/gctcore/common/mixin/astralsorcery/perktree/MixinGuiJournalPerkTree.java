package com.smd.gctcore.common.mixin.astralsorcery.perktree;

import com.smd.gctcore.client.DirectBufferCleaner;
import com.smd.gctcore.gctcore;
import hellfirepvp.astralsorcery.client.gui.GuiJournalPerkTree;
import hellfirepvp.astralsorcery.client.gui.journal.GuiScreenJournal;
import hellfirepvp.astralsorcery.client.gui.perk.BatchPerkContext;
import hellfirepvp.astralsorcery.client.util.BufferBatch;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.TreeMap;

/**
 * Converts {@link GuiJournalPerkTree}'s static 64 MiB off-heap {@link BufferBatch} buffers into
 * per-session buffers allocated only while the perk tree GUI is open and freed on {@link #onGuiClosed()}.
 */
@Mixin(GuiJournalPerkTree.class)
public abstract class MixinGuiJournalPerkTree extends GuiScreenJournal {

    @Shadow(remap = false)
    @Final
    @Mutable
    private static BufferBatch drawBufferConnections;

    @Shadow(remap = false)
    private static BatchPerkContext drawBuffer;

    @Shadow(remap = false)
    private static BatchPerkContext.TextureObjectGroup searchContext;

    @Shadow(remap = false)
    private static BatchPerkContext.TextureObjectGroup sealContext;

    protected MixinGuiJournalPerkTree(int bookmarkIndex) {
        super(bookmarkIndex);
    }

    @Shadow(remap = false)
    public static void initializeDrawBuffer() {
        throw new AssertionError();
    }

    @Redirect(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lhellfirepvp/astralsorcery/client/util/BufferBatch;make()Lhellfirepvp/astralsorcery/client/util/BufferBatch;",
                    remap = false
            ),
            remap = false
    )
    private static BufferBatch gctcore$deferStaticConnectionBuffer() {
        return null;
    }

    @Inject(method = "initGui()V", at = @At("HEAD"))
    private void gctcore$ensureBuffersOnInit(CallbackInfo ci) {
        gctcore$ensureDrawBuffers();
    }

    @Inject(method = "drawPerkTree(F)V", at = @At("HEAD"), remap = false)
    private void gctcore$ensureBuffersOnDraw(float partialTicks, CallbackInfo ci) {
        gctcore$ensureDrawBuffers();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        gctcore$releaseDrawBuffers();
    }

    @Unique
    private static void gctcore$ensureDrawBuffers() {
        boolean allocated = false;
        if (drawBufferConnections == null) {
            drawBufferConnections = BufferBatch.make();
            allocated = true;
        }
        if (drawBuffer == null || searchContext == null || sealContext == null) {
            initializeDrawBuffer();
            allocated = true;
        }
        if (allocated) {
            gctcore.LOGGER.debug("Allocated lazy AstralSorcery perk tree draw buffers");
        }
    }

    @Unique
    private static void gctcore$releaseDrawBuffers() {
        int released = 0;
        if (drawBufferConnections != null) {
            DirectBufferCleaner.freeBufferBuilder(
                    ((AccessorBufferBatch) (Object) drawBufferConnections).gctcore$getBuffer()
            );
            drawBufferConnections = null;
            released++;
        }
        if (drawBuffer != null) {
            TreeMap<BatchPerkContext.TextureObjectGroup, BufferBatch> groups =
                    ((AccessorBatchPerkContext) (Object) drawBuffer).gctcore$getBufferGroups();
            if (groups != null) {
                for (BufferBatch batch : groups.values()) {
                    if (batch != null) {
                        DirectBufferCleaner.freeBufferBuilder(
                                ((AccessorBufferBatch) (Object) batch).gctcore$getBuffer()
                        );
                        released++;
                    }
                }
                groups.clear();
            }
            drawBuffer = null;
        }
        searchContext = null;
        sealContext = null;
        if (released > 0) {
            gctcore.LOGGER.debug("Released {} AstralSorcery perk tree direct buffer batches on GUI close", released);
        }
    }
}
