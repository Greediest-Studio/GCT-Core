package com.smd.gctcore.common.mixin.defaultworldgenerator;

import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * DefaultWorldGenerator-port unregisters its world-tick listener from inside
 * the listener callback.  On the Cleanroom/Forge combination used by the
 * pack, that trips Forge's ListenerList bookkeeping and indexes an empty
 * listener array.  initDone is set before this call, so keeping the inert
 * listener registered is safe: subsequent ticks return immediately.
 */
@Pseudo
@Mixin(targets = "com.ezrol.terry.minecraft.defaultworldgenerator.WorldInitCommands", remap = false)
public abstract class MixinWorldInitCommands {

    @Redirect(
            method = {"onServerWorldTick", "unload"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/fml/common/eventhandler/EventBus;unregister(Ljava/lang/Object;)V"
            ),
            require = 0,
            remap = false
    )
    private void gctcore$skipUnsafeUnregister(EventBus eventBus, Object listener) {
        // The target sets initDone immediately before this call.  Leaving the
        // no-op listener registered avoids Forge 1.12 ListenerList's crash.
    }
}
