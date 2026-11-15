package com.smd.gctcore.common.mixin.astralsorcery.perktree;

import hellfirepvp.astralsorcery.client.ClientProxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Defers {@code GuiJournalPerkTree.initializeDrawBuffer()} from client startup until the
 * player actually opens the Astral Sorcery perk tree GUI, saving ~64 MiB of direct memory.
 */
@Mixin(value = ClientProxy.class, remap = false)
public abstract class MixinClientProxy {

    @Redirect(
            method = "init()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lhellfirepvp/astralsorcery/client/gui/GuiJournalPerkTree;initializeDrawBuffer()V",
                    remap = false
            ),
            remap = false
    )
    private void gctcore$deferPerkTreeBufferInit() {
        // Buffers are created lazily when GuiJournalPerkTree opens and released when it closes.
    }
}
