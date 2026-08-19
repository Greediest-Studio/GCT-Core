package com.smd.gctcore.common.botania;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum GctManaPoolTier implements IStringSerializable {
    JOETUNHEIM("joetunheim", 20_000_000, 100_000, 6_400),
    NIDAVELLIR("nidavellir", 50_000_000, 400_000, 30_000),
    VANAHEIM("vanaheim", 400_000_000, 6_400_000, 400_000);

    private final String name;
    private final int capacity;
    private final int spreaderCapacity;
    private final int burstMana;

    GctManaPoolTier(String name, int capacity, int spreaderCapacity, int burstMana) {
        this.name = name;
        this.capacity = capacity;
        this.spreaderCapacity = spreaderCapacity;
        this.burstMana = burstMana;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSpreaderCapacity() {
        return spreaderCapacity;
    }

    public int getBurstMana() {
        return burstMana;
    }

    public static GctManaPoolTier fromMeta(int meta) {
        GctManaPoolTier[] values = values();
        return meta >= 0 && meta < values.length ? values[meta] : JOETUNHEIM;
    }
}
