package com.smd.gctcore.common.util;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * Accesses Entity's web flag without relying on a Mixin refmap.
 *
 * <p>The development name is {@code isInWeb}; the 1.12.2 SRG runtime name is
 * {@code field_70134_J}. ReflectionHelper accepts both and the resolved field
 * is cached, so worn baubles do not perform a field lookup every tick.</p>
 */
public final class EntityWebState {
    private static final Field IS_IN_WEB = ReflectionHelper.findField(
            Entity.class, "isInWeb", "field_70134_J");

    private EntityWebState() { }

    public static void clear(Entity entity) {
        try {
            IS_IN_WEB.setBoolean(entity, false);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to clear Entity web state", exception);
        }
    }
}
