package com.smd.gctcore.common.mixin.whimcraft;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentSelectorTag;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Ensures any MMCE multiblock machine containing a WhimCraft ME aspect bus
 * runs in WorkMode.SEMI_SYNC.
 */
@Mixin(value = TileMultiblockMachineController.class, remap = false)
public abstract class MixinTileMultiblockMachineControllerWhimcraft {

    @Shadow(remap = false)
    protected TileMultiblockMachineController.WorkMode workMode;

    @Inject(
            method = "addComponent",
            at = @At("HEAD"),
            remap = false
    )
    private void gctcore$ensureSemiSyncForWhimcraft(
            MachineComponent<?> component,
            ComponentSelectorTag tag,
            TileEntity te,
            Map<Long, Map<TileEntity, ProcessingComponent<?>>> components,
            CallbackInfo ci
    ) {
        if (te != null && gctcore$isWhimcraftME(te)) {
            this.workMode = TileMultiblockMachineController.WorkMode.SEMI_SYNC;
        }
    }

    @Unique
    private static boolean gctcore$isWhimcraftME(TileEntity te) {
        String name = te.getClass().getName();
        return name.contains("TitleMEAspect") || (name.contains("whimcraft") && name.contains("ME"));
    }
}

