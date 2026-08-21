package com.smd.gctcore.common.botania;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum GctManaWoodVariant implements IStringSerializable {
    JOETUNHEIM("joetunheim"),
    NIDAVELLIR("nidavellir"),
    VANAHEIM("vanaheim"),
    JOETUNHEIM_GLOWING("joetunheimglowing"),
    NIDAVELLIR_GLOWING("nidavellirglowing"),
    VANAHEIM_GLOWING("vanaheimglowing");

    private final String name;

    GctManaWoodVariant(String name) {
        this.name = name;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    public boolean isGlowing() {
        return ordinal() >= JOETUNHEIM_GLOWING.ordinal();
    }

    public GctManaPoolTier getTier() {
        return GctManaPoolTier.fromMeta(ordinal() % GctManaPoolTier.values().length);
    }

    public static GctManaWoodVariant fromMeta(int meta) {
        GctManaWoodVariant[] values = values();
        return meta >= 0 && meta < values.length ? values[meta] : JOETUNHEIM;
    }
}
