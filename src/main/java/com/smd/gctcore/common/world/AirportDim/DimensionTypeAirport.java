package com.smd.gctcore.common.world.AirportDim;

import static net.minecraft.world.DimensionType.register;

public class DimensionTypeAirport {

    public static final net.minecraft.world.DimensionType Airport;

    static {
        Airport = register("airport", "_airport", -114514, WorldProviderAirport.class, false);
    }
}