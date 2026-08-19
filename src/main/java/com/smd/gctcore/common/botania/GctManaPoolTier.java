package com.smd.gctcore.common.botania;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum GctManaPoolTier implements IStringSerializable {
    JOETUNHEIM("joetunheim", 20_000_000),
    NIDAVELLIR("nidavellir", 50_000_000),
    VANAHEIM("vanaheim", 400_000_000);

    private final String name;
    private final int capacity;

    GctManaPoolTier(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public static GctManaPoolTier fromMeta(int meta) {
        GctManaPoolTier[] values = values();
        return meta >= 0 && meta < values.length ? values[meta] : JOETUNHEIM;
    }
}
