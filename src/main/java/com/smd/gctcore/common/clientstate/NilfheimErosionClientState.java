package com.smd.gctcore.common.clientstate;

public final class NilfheimErosionClientState {
    private NilfheimErosionClientState() {
    }

    public static volatile float progress;
    public static volatile boolean active;

    public static void set(float value, boolean visible) {
        progress = Math.max(0.0F, Math.min(1.0F, value));
        active = visible;
    }
}
