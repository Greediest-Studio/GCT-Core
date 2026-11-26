package com.smd.gctcore.common.integration.battletowers;

/** Numeric dimension rules shared by the natural world-generation guard. */
public final class BattleTowersDimensions {
    private BattleTowersDimensions() { }

    public static boolean allows(int dimension, boolean whitelist, int[] dimensions) {
        boolean listed = false;
        if (dimensions != null) {
            for (int candidate : dimensions) {
                if (candidate == dimension) {
                    listed = true;
                    break;
                }
            }
        }
        return whitelist == listed;
    }
}
