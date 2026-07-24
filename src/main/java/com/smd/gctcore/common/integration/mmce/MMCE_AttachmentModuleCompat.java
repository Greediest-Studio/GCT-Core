package com.smd.gctcore.common.integration.mmce;

import com.smd.gctcore.gctcore;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.TaggedPositionBlockArray;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/** Optional bridge to MMCE Complement's attachment-module API. */
final class MMCE_AttachmentModuleCompat {

    private static final String MOD_ID = "mmce_complement";
    private static final String ATTACHMENT_MACHINE_CLASS =
            "net.edwin.mmcecomplement.attachment.AttachmentMachine";
    private static final String GET_MODULES_METHOD = "mmceComplement$getAttachmentModules";
    private static final String GET_EFFECTIVE_PATTERN_METHOD = "getEffectivePattern";

    private MMCE_AttachmentModuleCompat() {
    }

    /**
     * Returns the selected module-only pattern, or {@code null} when the ID is
     * empty, invalid, or MMCE Complement is unavailable.
     */
    static TaggedPositionBlockArray findPattern(DynamicMachine machine, String moduleId) {
        if (machine == null || moduleId == null || moduleId.trim().isEmpty()
                || !Loader.isModLoaded(MOD_ID)) {
            return null;
        }

        try {
            Class<?> attachmentMachineClass = Class.forName(ATTACHMENT_MACHINE_CLASS, false,
                    machine.getClass().getClassLoader());
            if (!attachmentMachineClass.isInstance(machine)) {
                return null;
            }

            Method getModules = attachmentMachineClass.getMethod(GET_MODULES_METHOD);
            Object moduleMap = getModules.invoke(machine);
            if (!(moduleMap instanceof Map)) {
                return null;
            }

            Map<?, ?> modules = (Map<?, ?>) moduleMap;
            Object module = modules.get(moduleId.trim());
            if (module == null) {
                return null;
            }

            Method getEffectivePattern = module.getClass().getMethod(GET_EFFECTIVE_PATTERN_METHOD,
                    TaggedPositionBlockArray.class, Map.class);
            Object pattern = getEffectivePattern.invoke(module, machine.getPattern(), modules);
            return pattern instanceof TaggedPositionBlockArray
                    ? (TaggedPositionBlockArray) pattern
                    : null;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError e) {
            gctcore.LOGGER.warn("Failed to resolve MMCE attachment module '{}'; falling back to main",
                    moduleId, e);
            return null;
        }
    }
}
