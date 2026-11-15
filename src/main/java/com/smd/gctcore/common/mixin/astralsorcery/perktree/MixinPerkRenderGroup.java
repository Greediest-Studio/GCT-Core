package com.smd.gctcore.common.mixin.astralsorcery.perktree;

import hellfirepvp.astralsorcery.client.gui.perk.BatchPerkContext;
import hellfirepvp.astralsorcery.client.gui.perk.PerkRenderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Clears previously registered {@code TextureObjectGroup} entries before re-registering groups
 * against a fresh {@link BatchPerkContext} so reopening the perk tree does not retain closed contexts
 * or look up stale groups.
 */
@Mixin(value = PerkRenderGroup.class, remap = false)
public abstract class MixinPerkRenderGroup {

    @Shadow(remap = false)
    private List<BatchPerkContext.TextureObjectGroup> addedGroups;

    @Inject(
            method = "batchRegister(Lhellfirepvp/astralsorcery/client/gui/perk/BatchPerkContext;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void gctcore$clearPreviousGroups(BatchPerkContext ctx, CallbackInfo ci) {
        if (this.addedGroups != null) {
            this.addedGroups.clear();
        }
    }
}
