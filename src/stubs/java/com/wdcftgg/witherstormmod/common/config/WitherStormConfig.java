package com.wdcftgg.witherstormmod.common.config;

// Compile-time stub only - excluded from packaged jar by build.gradle
public final class WitherStormConfig {
    public enum ListMode {
        BLACKLIST,
        WHITELIST
    }

    public static ListMode summoningDimensionListMode = ListMode.BLACKLIST;
    public static String[] summoningDimensions = new String[0];
}

