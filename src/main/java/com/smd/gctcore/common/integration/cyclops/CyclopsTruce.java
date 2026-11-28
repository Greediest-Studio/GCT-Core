package com.smd.gctcore.common.integration.cyclops;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;

/** Cross-mod identification without linking either optional mod during class loading. */
public final class CyclopsTruce {
    private static final String ICE_AND_FIRE = "com.github.alexthe666.iceandfire.entity.EntityCyclops";
    private static final String CYCLOPS_TEK = "net.tangotek.cyclopstek.EntityCyclops";

    private CyclopsTruce() {}

    public static boolean areAllies(Entity first, Entity second) {
        int a = species(first), b = species(second);
        return a != 0 && b != 0 && a != b;
    }

    public static boolean friendlyDamage(Entity victim, DamageSource source) {
        return source != null && (areAllies(victim, source.getTrueSource())
                || areAllies(victim, source.getImmediateSource()));
    }

    public static boolean isIceAndFireCyclops(Entity entity) { return species(entity) == 1; }

    private static int species(Entity entity) {
        if (entity == null) return 0;
        for (Class<?> type = entity.getClass(); type != null; type = type.getSuperclass()) {
            if (ICE_AND_FIRE.equals(type.getName())) return 1;
            if (CYCLOPS_TEK.equals(type.getName())) return 2;
        }
        return 0;
    }
}
