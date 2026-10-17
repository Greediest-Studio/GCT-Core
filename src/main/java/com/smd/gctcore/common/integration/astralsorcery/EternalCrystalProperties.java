package com.smd.gctcore.common.integration.astralsorcery;

import hellfirepvp.astralsorcery.common.item.crystal.CrystalProperties;

/** Shared property factory for the eternal item and collector crystal variants. */
final class EternalCrystalProperties {
    private static final java.util.Random RANDOM = new java.util.Random();
    private static final CrystalProperties MAX = new CrystalProperties(
            EternalCrystalItem.MAX_SIZE, 100, 100, 0, -1);

    private EternalCrystalProperties() {}

    static CrystalProperties max() {
        return MAX;
    }

    static CrystalProperties random() {
        int size = Math.max(1, (RANDOM.nextInt(EternalCrystalItem.MAX_SIZE)
                + RANDOM.nextInt(EternalCrystalItem.MAX_SIZE)) / 2);
        int purity = 40 + RANDOM.nextInt(61);
        int collect = 50 + RANDOM.nextInt(26);
        return new CrystalProperties(size, purity, collect, 0, -1);
    }
}
