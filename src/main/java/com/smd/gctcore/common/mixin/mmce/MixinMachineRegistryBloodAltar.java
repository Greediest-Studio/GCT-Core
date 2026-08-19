package com.smd.gctcore.common.mixin.mmce;

import com.smd.gctcore.common.tile.blood_altar.BloodAltarMachine;
import com.smd.gctcore.misc.BlockRegistry;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.MachineRegistry;
import net.minecraft.command.ICommandSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

/**
 * Installs the dedicated controller and prevents MMCE from registering its
 * generated controller blocks. The machine definition itself remains entirely
 * under MMCE's normal JSON configuration directory.
 */
@Mixin(MachineRegistry.class)
public class MixinMachineRegistryBloodAltar {

    @Redirect(
            method = "getWaitForLoadMachines",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false),
            remap = false
    )
    @SuppressWarnings("unchecked")
    private static boolean gctcore$useBloodAltarController(final List<Object> machines, final Object candidate) {
        if (!(candidate instanceof DynamicMachine) || !BloodAltarMachine.isBloodAltar((DynamicMachine) candidate)) {
            return machines.add(candidate);
        }

        // Mirroring ECO's custom-controller registration prevents RegistryBlocks
        // from producing modularmachinery:blood_altar_factory_controller.  Do
        // not add this machine back to the list: that list is exactly what
        // MMCE uses to auto-register generated controller blocks.
        if (BlockRegistry.BLOOD_ALTAR_CONTROLLER != null) {
            BlockFactoryController.FACTORY_CONTROLLERS.put(
                    (DynamicMachine) candidate, BlockRegistry.BLOOD_ALTAR_CONTROLLER);
        }
        return true;
    }

    @Inject(method = "loadMachines", at = @At("RETURN"), cancellable = true, remap = false)
    private static void gctcore$configureBloodAltar(final ICommandSender sender,
                                                     final CallbackInfoReturnable<Collection<DynamicMachine>> callback) {
        for (final DynamicMachine machine : callback.getReturnValue()) {
            if (BloodAltarMachine.isBloodAltar(machine)) {
                BloodAltarMachine.configure(machine);
                if (BlockRegistry.BLOOD_ALTAR_CONTROLLER != null) {
                    BlockFactoryController.FACTORY_CONTROLLERS.put(
                            machine, BlockRegistry.BLOOD_ALTAR_CONTROLLER);
                }
            }
        }
    }
}
