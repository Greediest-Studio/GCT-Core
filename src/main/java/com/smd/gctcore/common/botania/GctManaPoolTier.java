package com.smd.gctcore.common.botania;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum GctManaPoolTier implements IStringSerializable {
    // The seven spreader tiers use a 0.1 block/tick velocity step.  The
    // Botaniverse tiers occupy 0.4, 0.7, 0.9 and 1.0, leaving these three
    // GCT tiers at 0.5, 0.6 and 0.8 respectively.
    JOETUNHEIM("joetunheim", 20_000_000, 100_000, 6_400, 0.5D),
    NIDAVELLIR("nidavellir", 50_000_000, 400_000, 30_000, 0.6D),
    VANAHEIM("vanaheim", 400_000_000, 6_400_000, 400_000, 0.8D);

    private final String name;
    private final int capacity;
    private final int spreaderCapacity;
    private final int burstMana;
    private final double burstVelocity;

    GctManaPoolTier(String name, int capacity, int spreaderCapacity, int burstMana,
                    double burstVelocity) {
        this.name = name;
        this.capacity = capacity;
        this.spreaderCapacity = spreaderCapacity;
        this.burstMana = burstMana;
        this.burstVelocity = burstVelocity;
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

    public double getBurstVelocity() {
        return burstVelocity;
    }

    public static GctManaPoolTier fromMeta(int meta) {
        GctManaPoolTier[] values = values();
        return meta >= 0 && meta < values.length ? values[meta] : JOETUNHEIM;
    }
}
