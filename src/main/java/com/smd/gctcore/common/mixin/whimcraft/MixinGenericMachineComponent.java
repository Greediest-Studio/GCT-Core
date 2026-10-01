package com.smd.gctcore.common.mixin.whimcraft;

import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;

/**
 * Marks GuGu's GenericMachineCompoment as not supporting async execution when
 * backed by a WhimCraft ME aspect bus.
 *
 * <p>This causes MMCE's structure check to downgrade the multiblock machine to
 * WorkMode.SEMI_SYNC, which executes recipe ticking and IO synchronously on the
 * Minecraft Server Thread, preventing off-thread AE2 ConcurrentModificationException.</p>
 */
@Pseudo
@Mixin(
        targets = "com.warmthdawn.mod.gugu_utils.modularmachenary.components.GenericMachineCompoment",
        remap = false
)
public abstract class MixinGenericMachineComponent extends MachineComponent<Object> {

    @Unique
    private static Field gctcore$consumableField;

    @Unique
    private static Field gctcore$generatableField;

    @Unique
    private static boolean gctcore$fieldsResolved;

    public MixinGenericMachineComponent() {
        super(null);
    }

    @Override
    public boolean isAsyncSupported() {
        if (gctcore$isUnsafeMEComponent()) {
            return false;
        }
        return super.isAsyncSupported();
    }

    @Unique
    private boolean gctcore$isUnsafeMEComponent() {
        if (!gctcore$fieldsResolved) {
            try {
                gctcore$consumableField = getClass().getDeclaredField("consumable");
                gctcore$consumableField.setAccessible(true);
            } catch (Throwable ignored) {
            }
            try {
                gctcore$generatableField = getClass().getDeclaredField("generatable");
                gctcore$generatableField.setAccessible(true);
            } catch (Throwable ignored) {
            }
            gctcore$fieldsResolved = true;
        }

        if (gctcore$consumableField != null) {
            try {
                Object c = gctcore$consumableField.get(this);
                if (c != null && gctcore$matchesUnsafe(c)) {
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }

        if (gctcore$generatableField != null) {
            try {
                Object g = gctcore$generatableField.get(this);
                if (g != null && gctcore$matchesUnsafe(g)) {
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }

        return false;
    }

    @Unique
    private static boolean gctcore$matchesUnsafe(Object obj) {
        String name = obj.getClass().getName();
        return name.contains("TitleMEAspect") || (name.contains("whimcraft") && name.contains("ME"));
    }
}

