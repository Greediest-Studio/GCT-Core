package com.smd.gctcore.common.mixin.tconstruct;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.gadgets.Exploder;

/**
 * TConstruct's EFLN exploder unregisters itself when its last work item is
 * finished. If the event bus has already removed the listener (for example
 * while a structure is being destroyed during the same world tick), the
 * 1.12.2 EventBus implementation indexes an empty listener array and throws
 * ArrayIndexOutOfBoundsException. Unregistering an already-unregistered
 * short-lived tick listener is harmless, so make this operation idempotent.
 */
@Mixin(value = Exploder.class, remap = false)
public class MixinExploder {

    @Redirect(
            method = "finish()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fml/common/eventhandler/EventBus;unregister(Ljava/lang/Object;)V"
            ),
            remap = false
    )
    private void gctcore_safeExploderUnregister(EventBus bus, Object listener) {
        try {
            bus.unregister(listener);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            // Forge 1.12's ListenerList.unregister is not idempotent. The
            // listener is already absent, which is the desired final state.
        }
    }
}
