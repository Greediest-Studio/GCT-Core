package com.smd.gctcore.common.integration.extendedcrafting;

public enum ExtendedCraftingTier {
    BASIC(1, 3, "basic"),
    ADVANCED(2, 5, "advanced"),
    ELITE(3, 7, "elite"),
    ULTIMATE(4, 9, "ultimate");

    private final int level;
    private final int gridSize;
    private final String id;

    ExtendedCraftingTier(int level, int gridSize, String id) {
        this.level = level;
        this.gridSize = gridSize;
        this.id = id;
    }

    public int level() {
        return level;
    }

    public int gridSize() {
        return gridSize;
    }

    public int patternSlots() {
        return 36;
    }

    public String id() {
        return id;
    }

    public static ExtendedCraftingTier byLevel(int level) {
        for (ExtendedCraftingTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }
        return BASIC;
    }
}
