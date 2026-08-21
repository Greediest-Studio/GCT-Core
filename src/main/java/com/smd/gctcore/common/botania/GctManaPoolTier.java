package com.smd.gctcore.common.botania;

import net.minecraft.util.IStringSerializable;

import javax.annotation.Nonnull;

public enum GctManaPoolTier implements IStringSerializable {
    // Botania's normal, dreamwood and Gaia spreaders are 0.4, 0.5 and
    // 0.8 block/tick.  These three tiers continue that ten-tier progression
    // at 1.0, 1.1 and 1.3 block/tick respectively.
    JOETUNHEIM("joetunheim", 20_000_000, 100_000, 6_400, 1.0D, 0xCD5600),
    NIDAVELLIR("nidavellir", 50_000_000, 400_000, 30_000, 1.1D, 0x171717),
    VANAHEIM("vanaheim", 400_000_000, 6_400_000, 400_000, 1.3D, 0x4D53B6);

    private final String name;
    private final int capacity;
    private final int spreaderCapacity;
    private final int burstMana;
    private final double burstVelocity;
    private final int color;

    GctManaPoolTier(String name, int capacity, int spreaderCapacity, int burstMana,
                    double burstVelocity, int color) {
        this.name = name;
        this.capacity = capacity;
        this.spreaderCapacity = spreaderCapacity;
        this.burstMana = burstMana;
        this.burstVelocity = burstVelocity;
        this.color = color;
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

    public int getColor() {
        return color;
    }

    public static GctManaPoolTier fromMeta(int meta) {
        GctManaPoolTier[] values = values();
        return meta >= 0 && meta < values.length ? values[meta] : JOETUNHEIM;
    }
}
