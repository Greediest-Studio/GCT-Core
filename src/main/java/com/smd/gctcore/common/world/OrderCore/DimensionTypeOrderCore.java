package com.smd.gctcore.common.world.OrderCore;

import static net.minecraft.world.DimensionType.register;

public class DimensionTypeOrderCore {
    public static final net.minecraft.world.DimensionType ordercore;
    public static final int NOTHINGNESS = 103 ;

    static {
        ordercore = register("ordercore", "_ordercore", 103, WorldProviderOrderCore.class, false);
    }
}
