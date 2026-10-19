package com.smd.gctcore.common.mixin.hammercore;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

/**
 * HammerLib fetches its link repository from pastebin/gist/zeith.org in the static initializer of
 * {@code ZeithLinkRepository$Storage}. The fetch only catches {@link Exception}, so a class loading
 * error inside the HTTP stack (e.g. {@code NoClassDefFoundError: HttpRequest$8}) escapes and crashes
 * game initialization via {@code ZeithLinkRepository.finishLoading()}.
 * <p>
 * Skipping the async fetch keeps the links baked into the mod and never touches the network.
 */
@Pseudo
@Mixin(targets = "com.zeitheron.hammercore.utils.ZeithLinkRepository$Storage$1", remap = false)
public abstract class MixinZeithLinkRepository {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;runAsync(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;",
                    remap = false
            )
    )
    private static CompletableFuture<Void> gct$skipLinkFetch(Runnable runnable) {
        return CompletableFuture.completedFuture(null);
    }
}
