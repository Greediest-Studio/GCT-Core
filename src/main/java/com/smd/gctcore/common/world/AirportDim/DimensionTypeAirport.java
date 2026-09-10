package com.smd.gctcore.common.world.AirportDim;

import net.minecraft.world.DimensionType;

import static net.minecraft.world.DimensionType.register;

public class DimensionTypeAirport {

    public static final DimensionType Airport;

    static {
        Airport = register("airport", "_airport", -114514, WorldProviderAirport.class, false);
    }
}