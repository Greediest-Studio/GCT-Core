package com.smd.gctcore.common.world.NothingnessDim;

import static net.minecraft.world.DimensionType.register;

public class DimensionTypeNothingness {
    public static final net.minecraft.world.DimensionType nothingness;

    static {
        nothingness = register("nothingness", "_nothingness", -114514, WorldProviderNothingness.class, false);
    }
}