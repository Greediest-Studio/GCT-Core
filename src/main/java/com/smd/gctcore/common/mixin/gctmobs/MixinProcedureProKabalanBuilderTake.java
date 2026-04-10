package com.smd.gctcore.common.mixin.gctmobs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels {@code ProcedureProKabalanBuilderTake.executeProcedure}.
 *
 * <p>The original procedure is triggered server-side via a network packet every time a player
 * takes an item from the KabalahBuilder output slot (slot 13) through the GUI. It decrements
 * one item from each of the 10 input slots (0-9).
 *
 * <p>Since {@link MixinTileEntityKabalahBuilder} now handles this consumption at the TileEntity
 * level (catching both GUI and pipe/hopper extraction), the GUI-driven procedure must be
 * cancelled to prevent double-consumption when a player empties the output slot.
 */
@Pseudo
@Mixin(targets = "net.mcreator.gctmobs.procedure.ProcedureProKabalanBuilderTake", remap = false)
public abstract class MixinProcedureProKabalanBuilderTake {

    @Inject(method = "executeProcedure", at = @At("HEAD"), cancellable = true)
    private static void gctcore$cancelKabalahTakeProcedure(java.util.Map<String, Object> dependencies, CallbackInfo ci) {
        // Input consumption is now handled uniformly by MixinTileEntityKabalahBuilder.
        // Cancelling here prevents double-deduction when a player takes items via the GUI.
        ci.cancel();
    }
}
